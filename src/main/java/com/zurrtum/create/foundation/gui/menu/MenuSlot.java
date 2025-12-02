package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MenuSlot extends Slot {
    public MenuSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return super.takeStack(Math.min(inventory.getMaxCount(getStack()), amount));
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return inventory.isValid(getIndex(), stack);
    }
}
