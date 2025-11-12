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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StockTickerInteractionHandler {
    public static InteractionResult interactWithLogisticsManager(Entity entity, Player player, InteractionHand hand) {
        BlockPos targetPos = getStockTickerPosition(entity);
        if (targetPos == null)
            return null;

        if (interactWithLogisticsManagerAt(player, player.level(), targetPos)) {
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public static boolean interactWithLogisticsManagerAt(Player player, Level level, BlockPos targetPos) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.is(AllItems.SHOPPING_LIST)) {
            interactWithShop(player, level, targetPos, mainHandItem);
            return true;
        }

        if (level.isClientSide())
            return true;
        if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity stbe))
            return false;

        if (!stbe.behaviour.mayInteract(player)) {
            player.displayClientMessage(Component.translatable("create.stock_keeper.locked").withStyle(ChatFormatting.RED), true);
            return true;
        }

        if (player instanceof ServerPlayer sp) {
            MenuProvider.openHandledScreen(sp, stbe::createRequestMenu);
            stbe.getRecentSummary().divideAndSendTo(sp, targetPos);
        }

        return true;
    }

    private static void interactWithShop(Player player, Level level, BlockPos targetPos, ItemStack mainHandItem) {
        if (level.isClientSide())
            return;
        if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE))
            return;

        ShoppingList list = ShoppingListItem.getList(mainHandItem);
        if (list == null)
            return;

        if (!tickerBE.behaviour.freqId.equals(list.shopNetwork())) {
            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            player.displayClientMessage(Component.translatable("create.stock_keeper.wrong_network").withStyle(ChatFormatting.RED), true);
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

            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            player.displayClientMessage(Component.translatable("create.stock_keeper.stock_level_too_low").withStyle(ChatFormatting.RED), true);
            return;
        }

        // Check space in stock ticker
        int occupiedSlots = 0;
        for (BigItemStack entry : paymentEntries.getStacksByCount())
            occupiedSlots += Mth.ceil(entry.count / (float) entry.stack.getMaxStackSize());
        Container receivedPayments = tickerBE.receivedPayments;
        for (int i = 0, size = receivedPayments.getContainerSize(); i < size; i++)
            if (receivedPayments.getItem(i).isEmpty())
                occupiedSlots--;

        if (occupiedSlots > 0) {
            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            player.displayClientMessage(Component.translatable("create.stock_keeper.cash_register_full").withStyle(ChatFormatting.RED), true);
            return;
        }

        // Transfer payment to stock ticker
        Inventory playerInventory = player.getInventory();
        for (boolean simulate : Iterate.trueAndFalse) {
            InventorySummary tally = paymentEntries.copy();
            List<ItemStack> toTransfer = new ArrayList<>();

            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                ItemStack item = playerInventory.getItem(i);
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
                playerInventory.setItem(i, newStackSize == 0 ? ItemStack.EMPTY : item.copyWithCount(newStackSize));
                toTransfer.add(item.copyWithCount(toRemove));
            }

            if (simulate && tally.getTotalCount() != 0) {
                AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                player.displayClientMessage(Component.translatable("create.stock_keeper.too_broke").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (simulate)
                continue;

            receivedPayments.insert(toTransfer);
        }

        tickerBE.broadcastPackageRequest(RequestType.PLAYER, order, null, ShoppingListItem.getAddress(mainHandItem));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (!order.isEmpty())
            AllSoundEvents.STOCK_TICKER_TRADE.playOnServer(level, tickerBE.getBlockPos());
    }

    public static BlockPos getStockTickerPosition(Entity entity) {
        Entity rootVehicle = entity.getRootVehicle();
        if (!(rootVehicle instanceof SeatEntity))
            return null;
        if (!(entity instanceof LivingEntity))
            return null;
        if (entity.getType() == AllEntityTypes.PACKAGE)
            return null;

        BlockPos pos = entity.blockPosition();
        int stations = 0;
        BlockPos targetPos = null;

        Level world = entity.level();
        for (Direction d : Iterate.horizontalDirections) {
            for (int y : Iterate.zeroAndOne) {
                BlockPos workstationPos = pos.relative(d).above(y);
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