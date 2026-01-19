package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class StockKeeperCategoryMenu extends MenuBase<StockTickerBlockEntity> {

    public boolean slotsActive = true;
    public ItemStackHandler proxyInventory;

    public StockKeeperCategoryMenu(int id, Inventory inv, StockTickerBlockEntity contentHolder) {
        super(AllMenuTypes.STOCK_KEEPER_CATEGORY, id, inv, contentHolder);
    }

    @Override
    protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {
        proxyInventory = new ItemStackHandler(1);
    }

    @Override
    protected void addSlots() {
        addSlot(new InactiveItemHandlerSlot(proxyInventory, 0, 16, 24));
        addPlayerSlots(18, 106);
    }

    @Override
    protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
        return new InactiveSlot(inventory, index, x, y);
    }

    @Override
    protected void saveData(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public boolean stillValid(Player player) {
        return !contentHolder.isRemoved() && player.position()
            .closerThan(Vec3.atCenterOf(contentHolder.getBlockPos()), player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 4);
    }

    class InactiveSlot extends Slot {
        public InactiveSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isActive() {
            return slotsActive;
        }
    }

    class InactiveItemHandlerSlot extends Slot {
        public InactiveItemHandlerSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && (stack.isEmpty() || stack.getItem() instanceof FilterItem);
        }

        @Override
        public boolean isActive() {
            return slotsActive;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getItem();
        int size = 1;
        boolean success;
        if (index < size) {
            success = !moveItemStackTo(stack, size, slots.size(), true);
        } else
            success = !moveItemStackTo(stack, 0, size, false);

        return success ? ItemStack.EMPTY : stack;
    }

}
