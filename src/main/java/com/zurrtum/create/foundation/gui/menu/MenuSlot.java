package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuSlot extends Slot {
    public MenuSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public ItemStack remove(int amount) {
        return super.remove(Math.min(container.getMaxStackSize(getItem()), amount));
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return container.canPlaceItem(getContainerSlot(), itemStack);
    }
}
