package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.*;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.component.AutoRequestData;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import com.zurrtum.create.infrastructure.packet.c2s.LogisticalStockRequestPacket;
import com.zurrtum.create.infrastructure.packet.s2c.RemoveBlockEntityPacket;
import com.zurrtum.create.infrastructure.packet.s2c.ShopUpdatePacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TableClothBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public AbstractComputerBehaviour computerBehaviour;

    public AutoRequestData requestData;
    public List<ItemStack> manuallyAddedItems;
    public UUID owner;

    public Direction facing;
    public boolean sideOccluded;
    public ServerFilteringBehaviour priceTag;

    private List<ItemStack> renderedItemsForShop;

    public TableClothBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TABLE_CLOTH, pos, state);
        manuallyAddedItems = new ArrayList<>();
        requestData = new AutoRequestData();
        owner = null;
        facing = Direction.SOUTH;
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(priceTag = new ServerTableClothFilteringBehaviour(this));
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    public List<ItemStack> getItemsForRender() {
        if (isShop()) {
            if (renderedItemsForShop == null)
                renderedItemsForShop = requestData.encodedRequest().stacks().stream().map(b -> b.stack).limit(4).toList();
            return renderedItemsForShop;
        }

        return manuallyAddedItems;
    }

    public void invalidateItemsForRender() {
        renderedItemsForShop = null;
    }

    public void notifyShopUpdate() {
        if (world instanceof ServerWorld serverLevel) {
            Packet<?> packet = new ShopUpdatePacket(pos);
            for (ServerPlayerEntity player : serverLevel.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(new ChunkPos(pos), false)) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        BlockPos relativePos = pos.offset(facing);
        sideOccluded = world.getBlockState(relativePos)
            .isIn(AllBlockTags.TABLE_CLOTHS) || Block.isFaceFullSquare(
            world.getBlockState(relativePos.down()).getCullingShape(),
            facing.getOpposite()
        );
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(1);
    }

    public boolean isShop() {
        return !requestData.encodedRequest().isEmpty();
    }

    public ActionResult use(PlayerEntity player, BlockHitResult ray) {
        if (isShop())
            return useShop(player);

        ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);

        if (heldItem.isEmpty()) {
            if (manuallyAddedItems.isEmpty())
                return ActionResult.SUCCESS;
            player.setStackInHand(Hand.MAIN_HAND, manuallyAddedItems.remove(manuallyAddedItems.size() - 1));
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.5f, 1f);

            if (manuallyAddedItems.isEmpty() && !computerBehaviour.hasAttachedComputer()) {
                world.setBlockState(pos, getCachedState().with(TableClothBlock.HAS_BE, false), Block.NOTIFY_ALL);
                if (world instanceof ServerWorld serverLevel) {
                    Packet<?> packet = new RemoveBlockEntityPacket(pos);
                    for (ServerPlayerEntity serverPlayer : serverLevel.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(
                        new ChunkPos(pos),
                        false
                    )) {
                        serverPlayer.networkHandler.sendPacket(packet);
                    }
                }
            } else
                notifyUpdate();

            return ActionResult.SUCCESS;
        }

        if (manuallyAddedItems.size() >= 4)
            return ActionResult.SUCCESS;

        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.5f, 1f);
        manuallyAddedItems.add(heldItem.copyWithCount(1));
        facing = player.getHorizontalFacing().getOpposite();
        heldItem.decrement(1);
        if (heldItem.isEmpty())
            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        notifyUpdate();
        return ActionResult.SUCCESS;
    }

    public ActionResult useShop(PlayerEntity player) {
        ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack prevListItem = ItemStack.EMPTY;
        boolean addOntoList = false;

        // Remove other lists from inventory
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getStack(i);
            if (!item.isOf(AllItems.SHOPPING_LIST))
                continue;
            prevListItem = item;
            addOntoList = true;
            player.getInventory().setStack(i, ItemStack.EMPTY);
        }

        // add onto existing list if in hand
        if (itemInHand.isOf(AllItems.SHOPPING_LIST)) {
            prevListItem = itemInHand;
            addOntoList = true;
        }

        if (!itemInHand.isEmpty() && !addOntoList) {
            player.sendMessage(Text.translatable("create.stock_keeper.shopping_list_empty_hand"), true);
            AllSoundEvents.DENY.playOnServer(world, pos, 0.5f, 1);
            return ActionResult.SUCCESS;
        }

        if (getPaymentItem().isEmpty()) {
            player.sendMessage(Text.translatable("create.stock_keeper.no_price_set"), true);
            AllSoundEvents.DENY.playOnServer(world, pos, 0.5f, 1);
            return ActionResult.SUCCESS;
        }

        UUID tickerID = null;
        BlockPos tickerPos = requestData.targetOffset().add(pos);
        if (world.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe && stbe.isKeeperPresent())
            tickerID = stbe.behaviour.freqId;

        int stockLevel = getStockLevelForTrade(ShoppingListItem.getList(prevListItem));

        if (tickerID == null) {
            player.sendMessage(Text.translatable("create.stock_keeper.keeper_missing").formatted(Formatting.RED), true);
            AllSoundEvents.DENY.playOnServer(world, pos, 0.5f, 1);
            return ActionResult.SUCCESS;
        }

        if (stockLevel == 0) {
            player.sendMessage(Text.translatable("create.stock_keeper.out_of_stock").formatted(Formatting.RED), true);
            AllSoundEvents.DENY.playOnServer(world, pos, 0.5f, 1);
            if (!prevListItem.isEmpty()) {
                if (player.getStackInHand(Hand.MAIN_HAND).isEmpty())
                    player.setStackInHand(Hand.MAIN_HAND, prevListItem);
                else
                    player.getInventory().offerOrDrop(prevListItem);
            }

            return ActionResult.SUCCESS;
        }

        ShoppingList list = new ShoppingList(new ArrayList<>(), owner, tickerID);

        if (addOntoList) {
            ShoppingList prevList = ShoppingListItem.getList(prevListItem).duplicate();
            if (owner.equals(prevList.shopOwner()) && tickerID.equals(prevList.shopNetwork()))
                list = prevList;
            else
                addOntoList = false;
        }

        if (list.getPurchases(pos) >= stockLevel) {
            for (IntAttached<BlockPos> entry : list.purchases())
                if (pos.equals(entry.getValue()))
                    entry.setFirst(Math.min(stockLevel, entry.getFirst()));

            player.sendMessage(Text.translatable("create.stock_keeper.limited_stock").formatted(Formatting.RED), true);
        } else {
            AllSoundEvents.CONFIRM_2.playOnServer(world, pos, 0.5f, 1.0f);

            ShoppingList.Mutable mutable = new ShoppingList.Mutable(list);
            mutable.addPurchases(pos, 1);
            list = mutable.toImmutable();

            if (!addOntoList)
                player.sendMessage(Text.translatable("create.stock_keeper.use_list_to_add_purchases").withColor(0xeeeeee), true);
            if (!addOntoList)
                world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 1.5f);
        }

        ItemStack newListItem = ShoppingListItem.saveList(AllItems.SHOPPING_LIST.getDefaultStack(), list, requestData.encodedTargetAddress());

        if (player.getStackInHand(Hand.MAIN_HAND).isEmpty())
            player.setStackInHand(Hand.MAIN_HAND, newListItem);
        else
            player.getInventory().offerOrDrop(newListItem);

        return ActionResult.SUCCESS;
    }

    public int getStockLevelForTrade(@Nullable ShoppingList otherPurchases) {
        BlockPos tickerPos = requestData.targetOffset().add(pos);
        if (!(world.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe))
            return 0;

        InventorySummary recentSummary;

        if (world.isClient()) {
            if (stbe.getTicksSinceLastUpdate() > 15) {
                stbe.resetTicksSinceLastUpdate();
                AllClientHandle.INSTANCE.sendPacket(new LogisticalStockRequestPacket(stbe.getPos()));
            }
            recentSummary = stbe.getLastClientsideStockSnapshotAsSummary();
        } else
            recentSummary = stbe.getRecentSummary();

        if (recentSummary == null)
            return 0;

        InventorySummary modifierSummary = new InventorySummary();
        if (otherPurchases != null)
            modifierSummary = otherPurchases.bakeEntries(world, pos).getFirst();

        int smallestQuotient = Integer.MAX_VALUE;
        for (BigItemStack entry : requestData.encodedRequest().stacks())
            if (entry.count > 0)
                smallestQuotient = Math.min(
                    smallestQuotient,
                    (recentSummary.getCountOf(entry.stack) - modifierSummary.getCountOf(entry.stack)) / entry.count
                );

        return smallestQuotient;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.put("Items", CreateCodecs.ITEM_LIST_CODEC, manuallyAddedItems);
        view.put("Facing", Direction.CODEC, facing);
        view.put("RequestData", AutoRequestData.CODEC, requestData);
        if (owner != null)
            view.put("OwnerUUID", Uuids.INT_STREAM_CODEC, owner);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        manuallyAddedItems.clear();
        view.read("Items", CreateCodecs.ITEM_LIST_CODEC).ifPresent(list -> manuallyAddedItems.addAll(list));
        requestData = view.read("RequestData", AutoRequestData.CODEC).orElseGet(AutoRequestData::new);
        owner = view.read("OwnerUUID", Uuids.INT_STREAM_CODEC).orElse(null);
        facing = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
    }

    @Override
    public void destroy() {
        super.destroy();
        manuallyAddedItems.forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        manuallyAddedItems.clear();
    }

    public ItemStack getPaymentItem() {
        return priceTag.getFilter();
    }

    public int getPaymentAmount() {
        return priceTag.getFilter().isEmpty() ? 1 : priceTag.count;
    }

    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        facing = transform.mirrorFacing(facing);
        if (transform.rotationAxis == Direction.Axis.Y)
            facing = transform.rotateFacing(facing);
        notifyUpdate();
    }
}
