package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class StockKeeperCategoryMenu extends MenuBase<StockTickerBlockEntity> {

    public boolean slotsActive = true;
    public ItemStackHandler proxyInventory;

    public StockKeeperCategoryMenu(int id, PlayerInventory inv, StockTickerBlockEntity contentHolder) {
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
    protected Slot createPlayerSlot(PlayerInventory inventory, int index, int x, int y) {
        return new InactiveSlot(inventory, index, x, y);
    }

    @Override
    protected void saveData(StockTickerBlockEntity contentHolder) {
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return !contentHolder.isRemoved() && player.getEntityPos()
            .isInRange(Vec3d.ofCenter(contentHolder.getPos()), player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 4);
    }

    class InactiveSlot extends Slot {
        public InactiveSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isEnabled() {
            return slotsActive;
        }
    }

    class InactiveItemHandlerSlot extends Slot {
        public InactiveItemHandlerSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(@NotNull ItemStack stack) {
            return super.canInsert(stack) && (stack.isEmpty() || stack.getItem() instanceof FilterItem);
        }

        @Override
        public boolean isEnabled() {
            return slotsActive;
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity pPlayer, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasStack())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getStack();
        int size = 1;
        boolean success;
        if (index < size) {
            success = !insertItem(stack, size, slots.size(), true);
        } else
            success = !insertItem(stack, 0, size, false);

        return success ? ItemStack.EMPTY : stack;
    }

}
