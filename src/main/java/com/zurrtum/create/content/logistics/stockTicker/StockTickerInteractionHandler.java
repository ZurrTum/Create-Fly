package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.tableCloth.ShoppingListItem;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.ShoppingList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StockTickerInteractionHandler {
    public static ActionResult interactWithLogisticsManager(Entity entity, PlayerEntity player, Hand hand) {
        BlockPos targetPos = getStockTickerPosition(entity);
        if (targetPos == null)
            return null;

        if (interactWithLogisticsManagerAt(player, player.getEntityWorld(), targetPos)) {
            return ActionResult.SUCCESS;
        }
        return null;
    }

    public static boolean interactWithLogisticsManagerAt(PlayerEntity player, World level, BlockPos targetPos) {
        ItemStack mainHandItem = player.getMainHandStack();

        if (mainHandItem.isOf(AllItems.SHOPPING_LIST)) {
            interactWithShop(player, level, targetPos, mainHandItem);
            return true;
        }

        if (level.isClient())
            return true;
        if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity stbe))
            return false;

        if (!stbe.behaviour.mayInteract(player)) {
            player.sendMessage(Text.translatable("create.stock_keeper.locked").formatted(Formatting.RED), true);
            return true;
        }

        if (player instanceof ServerPlayerEntity sp) {
            MenuProvider.openHandledScreen(sp, stbe::createRequestMenu);
            stbe.getRecentSummary().divideAndSendTo(sp, targetPos);
        }

        return true;
    }

    private static void interactWithShop(PlayerEntity player, World level, BlockPos targetPos, ItemStack mainHandItem) {
        if (level.isClient())
            return;
        if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE))
            return;

        ShoppingList list = ShoppingListItem.getList(mainHandItem);
        if (list == null)
            return;

        if (!tickerBE.behaviour.freqId.equals(list.shopNetwork())) {
            AllSoundEvents.DENY.playOnServer(level, player.getBlockPos());
            player.sendMessage(Text.translatable("create.stock_keeper.wrong_network").formatted(Formatting.RED), true);
            return;
        }

        Couple<InventorySummary> bakeEntries = list.bakeEntries(level, null);
        InventorySummary paymentEntries = bakeEntries.getSecond();
        InventorySummary orderEntries = bakeEntries.getFirst();
        PackageOrder order = new PackageOrder(orderEntries.getStacksByCount());

        // Must be up-to-date
        tickerBE.getAccurateSummary();

        // Check stock levels
        InventorySummary recentSummary = tickerBE.getRecentSummary();
        for (BigItemStack entry : order.stacks()) {
            if (recentSummary.getCountOf(entry.stack) >= entry.count)
                continue;

            AllSoundEvents.DENY.playOnServer(level, player.getBlockPos());
            player.sendMessage(Text.translatable("create.stock_keeper.stock_level_too_low").formatted(Formatting.RED), true);
            return;
        }

        // Check space in stock ticker
        int occupiedSlots = 0;
        for (BigItemStack entry : paymentEntries.getStacksByCount())
            occupiedSlots += MathHelper.ceil(entry.count / (float) entry.stack.getMaxCount());
        Inventory receivedPayments = tickerBE.receivedPayments;
        for (int i = 0, size = receivedPayments.size(); i < size; i++)
            if (receivedPayments.getStack(i).isEmpty())
                occupiedSlots--;

        if (occupiedSlots > 0) {
            AllSoundEvents.DENY.playOnServer(level, player.getBlockPos());
            player.sendMessage(Text.translatable("create.stock_keeper.cash_register_full").formatted(Formatting.RED), true);
            return;
        }

        // Transfer payment to stock ticker
        PlayerInventory playerInventory = player.getInventory();
        for (boolean simulate : Iterate.trueAndFalse) {
            InventorySummary tally = paymentEntries.copy();
            List<ItemStack> toTransfer = new ArrayList<>();

            for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
                ItemStack item = playerInventory.getStack(i);
                if (item.isEmpty())
                    continue;
                int countOf = tally.getCountOf(item);
                if (countOf == 0)
                    continue;
                int toRemove = Math.min(item.getCount(), countOf);
                tally.add(item, -toRemove);

                if (simulate)
                    continue;

                int newStackSize = item.getCount() - toRemove;
                playerInventory.setStack(i, newStackSize == 0 ? ItemStack.EMPTY : item.copyWithCount(newStackSize));
                toTransfer.add(item.copyWithCount(toRemove));
            }

            if (simulate && tally.getTotalCount() != 0) {
                AllSoundEvents.DENY.playOnServer(level, player.getBlockPos());
                player.sendMessage(Text.translatable("create.stock_keeper.too_broke").formatted(Formatting.RED), true);
                return;
            }

            if (simulate)
                continue;

            receivedPayments.insert(toTransfer);
        }

        tickerBE.broadcastPackageRequest(RequestType.PLAYER, order, null, ShoppingListItem.getAddress(mainHandItem));
        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        if (!order.isEmpty())
            AllSoundEvents.STOCK_TICKER_TRADE.playOnServer(level, tickerBE.getPos());
    }

    public static BlockPos getStockTickerPosition(Entity entity) {
        Entity rootVehicle = entity.getRootVehicle();
        if (!(rootVehicle instanceof SeatEntity))
            return null;
        if (!(entity instanceof LivingEntity))
            return null;
        if (entity.getType() == AllEntityTypes.PACKAGE)
            return null;

        BlockPos pos = entity.getBlockPos();
        int stations = 0;
        BlockPos targetPos = null;

        World world = entity.getEntityWorld();
        for (Direction d : Iterate.horizontalDirections) {
            for (int y : Iterate.zeroAndOne) {
                BlockPos workstationPos = pos.offset(d).up(y);
                if (!(world.getBlockState(workstationPos).getBlock() instanceof StockTickerBlock))
                    continue;
                targetPos = workstationPos;
                stations++;
            }
        }

        if (stations != 1)
            return null;
        return targetPos;
    }

}