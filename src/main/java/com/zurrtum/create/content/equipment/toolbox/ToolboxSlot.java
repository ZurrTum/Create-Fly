package com.zurrtum.create.content.equipment.toolbox;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ToolboxSlot extends Slot {
    private final ToolboxMenu toolboxMenu;
    private final boolean isVisible;

    public ToolboxSlot(ToolboxMenu menu, Inventory inventory, int index, int x, int y, boolean isVisible) {
        super(inventory, index, x, y);
        this.toolboxMenu = menu;
        this.isVisible = isVisible;
    }

    @Override
    public boolean isEnabled() {
        return !toolboxMenu.renderPass && isVisible;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !stack.isEmpty() && inventory.isValid(getIndex(), stack);
    }
}
