package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.*;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.data.IntAttached;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TableClothBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    //TODO
    //    public AbstractComputerBehaviour computerBehaviour;

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

    //TODO
    //    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    //        if (Mods.COMPUTERCRAFT.isLoaded()) {
    //            event.registerBlockEntity(
    //                PeripheralCapability.get(),
    //                AllBlockEntityTypes.TABLE_CLOTH.get(),
    //                (be, context) -> be.computerBehaviour.getPeripheralCapability()
    //            );
    //        }
    //    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(priceTag = new ServerTableClothFilteringBehaviour(this));
        //TODO
        //        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
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
        if (level instanceof ServerLevel serverLevel) {
            Packet<?> packet = new ShopUpdatePacket(worldPosition);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
                player.connection.send(packet);
            }
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        BlockPos relativePos = worldPosition.relative(facing);
        sideOccluded = level.getBlockState(relativePos)
            .is(AllBlockTags.TABLE_CLOTHS) || Block.isFaceFull(
            level.getBlockState(relativePos.below()).getOcclusionShape(),
            facing.getOpposite()
        );
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    public boolean isShop() {
        return !requestData.encodedRequest().isEmpty();
    }

    public InteractionResult use(Player player, BlockHitResult ray) {
        if (isShop())
            return useShop(player);

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (heldItem.isEmpty()) {
            if (manuallyAddedItems.isEmpty())
                return InteractionResult.SUCCESS;
            player.setItemInHand(InteractionHand.MAIN_HAND, manuallyAddedItems.remove(manuallyAddedItems.size() - 1));
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5f, 1f);

            if (manuallyAddedItems.isEmpty()/* && !computerBehaviour.hasAttachedComputer()*/) {
                level.setBlock(worldPosition, getBlockState().setValue(TableClothBlock.HAS_BE, false), Block.UPDATE_ALL);
                if (level instanceof ServerLevel serverLevel) {
                    Packet<?> packet = new RemoveBlockEntityPacket(worldPosition);
                    for (ServerPlayer serverPlayer : serverLevel.getChunkSource().chunkMap.getPlayers(
                        new ChunkPos(worldPosition),
                        false
                    )) {
                        serverPlayer.connection.send(packet);
                    }
                }
            } else
                notifyUpdate();

            return InteractionResult.SUCCESS;
        }

        if (manuallyAddedItems.size() >= 4)
            return InteractionResult.SUCCESS;

        level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5f, 1f);
        manuallyAddedItems.add(heldItem.copyWithCount(1));
        facing = player.getDirection().getOpposite();
        heldItem.shrink(1);
        if (heldItem.isEmpty())
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        notifyUpdate();
        return InteractionResult.SUCCESS;
    }

    public InteractionResult useShop(Player player) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack prevListItem = ItemStack.EMPTY;
        boolean addOntoList = false;

        // Remove other lists from inventory
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!item.is(AllItems.SHOPPING_LIST))
                continue;
            prevListItem = item;
            addOntoList = true;
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        // add onto existing list if in hand
        if (itemInHand.is(AllItems.SHOPPING_LIST)) {
            prevListItem = itemInHand;
            addOntoList = true;
        }

        if (!itemInHand.isEmpty() && !addOntoList) {
            player.displayClientMessage(Component.translatable("create.stock_keeper.shopping_list_empty_hand"), true);
            AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
            return InteractionResult.SUCCESS;
        }

        if (getPaymentItem().isEmpty()) {
            player.displayClientMessage(Component.translatable("create.stock_keeper.no_price_set"), true);
            AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
            return InteractionResult.SUCCESS;
        }

        UUID tickerID = null;
        BlockPos tickerPos = requestData.targetOffset().offset(worldPosition);
        if (level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe && stbe.isKeeperPresent())
            tickerID = stbe.behaviour.freqId;

        int stockLevel = getStockLevelForTrade(ShoppingListItem.getList(prevListItem));

        if (tickerID == null) {
            player.displayClientMessage(Component.translatable("create.stock_keeper.keeper_missing").withStyle(ChatFormatting.RED), true);
            AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
            return InteractionResult.SUCCESS;
        }

        if (stockLevel == 0) {
            player.displayClientMessage(Component.translatable("create.stock_keeper.out_of_stock").withStyle(ChatFormatting.RED), true);
            AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
            if (!prevListItem.isEmpty()) {
                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                    player.setItemInHand(InteractionHand.MAIN_HAND, prevListItem);
                else
                    player.getInventory().placeItemBackInInventory(prevListItem);
            }

            return InteractionResult.SUCCESS;
        }

        ShoppingList list = new ShoppingList(new ArrayList<>(), owner, tickerID);

        if (addOntoList) {
            ShoppingList prevList = ShoppingListItem.getList(prevListItem).duplicate();
            if (owner.equals(prevList.shopOwner()) && tickerID.equals(prevList.shopNetwork()))
                list = prevList;
            else
                addOntoList = false;
        }

        if (list.getPurchases(worldPosition) >= stockLevel) {
            for (IntAttached<BlockPos> entry : list.purchases())
                if (worldPosition.equals(entry.getValue()))
                    entry.setFirst(Math.min(stockLevel, entry.getFirst()));

            player.displayClientMessage(Component.translatable("create.stock_keeper.limited_stock").withStyle(ChatFormatting.RED), true);
        } else {
            AllSoundEvents.CONFIRM_2.playOnServer(level, worldPosition, 0.5f, 1.0f);

            ShoppingList.Mutable mutable = new ShoppingList.Mutable(list);
            mutable.addPurchases(worldPosition, 1);
            list = mutable.toImmutable();

            if (!addOntoList)
                player.displayClientMessage(Component.translatable("create.stock_keeper.use_list_to_add_purchases").withColor(0xeeeeee), true);
            if (!addOntoList)
                level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 1.5f);
        }

        ItemStack newListItem = ShoppingListItem.saveList(AllItems.SHOPPING_LIST.getDefaultInstance(), list, requestData.encodedTargetAddress());

        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
            player.setItemInHand(InteractionHand.MAIN_HAND, newListItem);
        else
            player.getInventory().placeItemBackInInventory(newListItem);

        return InteractionResult.SUCCESS;
    }

    public int getStockLevelForTrade(@Nullable ShoppingList otherPurchases) {
        BlockPos tickerPos = requestData.targetOffset().offset(worldPosition);
        if (!(level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe))
            return 0;

        InventorySummary recentSummary;

        if (level.isClientSide()) {
            if (stbe.getTicksSinceLastUpdate() > 15) {
                stbe.resetTicksSinceLastUpdate();
                AllClientHandle.INSTANCE.sendPacket(new LogisticalStockRequestPacket(stbe.getBlockPos()));
            }
            recentSummary = stbe.getLastClientsideStockSnapshotAsSummary();
        } else
            recentSummary = stbe.getRecentSummary();

        if (recentSummary == null)
            return 0;

        InventorySummary modifierSummary = new InventorySummary();
        if (otherPurchases != null)
            modifierSummary = otherPurchases.bakeEntries(level, worldPosition).getFirst();

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
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store("Items", CreateCodecs.ITEM_LIST_CODEC, manuallyAddedItems);
        view.store("Facing", Direction.CODEC, facing);
        view.store("RequestData", AutoRequestData.CODEC, requestData);
        if (owner != null)
            view.store("OwnerUUID", UUIDUtil.CODEC, owner);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        manuallyAddedItems.clear();
        view.read("Items", CreateCodecs.ITEM_LIST_CODEC).ifPresent(list -> manuallyAddedItems.addAll(list));
        requestData = view.read("RequestData", AutoRequestData.CODEC).orElseGet(AutoRequestData::new);
        owner = view.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);
        facing = view.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
    }

    @Override
    public void destroy() {
        super.destroy();
        manuallyAddedItems.forEach(stack -> Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack));
        manuallyAddedItems.clear();
    }

    public ItemStack getPaymentItem() {
        return priceTag.getFilter();
    }

    public int getPaymentAmount() {
        return priceTag.getFilter().isEmpty() ? 1 : priceTag.count;
    }

    //TODO
    //    @Override
    //    public void invalidate() {
    //        super.invalidate();
    //        computerBehaviour.removePeripheral();
    //    }

    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        facing = transform.mirrorFacing(facing);
        if (transform.rotationAxis == Direction.Axis.Y)
            facing = transform.rotateFacing(facing);
        notifyUpdate();
    }
}
