package com.zurrtum.create.content.equipment.toolbox;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ToolboxSlot extends Slot {
    private final ToolboxMenu toolboxMenu;
    private final boolean isVisible;

    public ToolboxSlot(ToolboxMenu menu, Container inventory, int index, int x, int y, boolean isVisible) {
        super(inventory, index, x, y);
        this.toolboxMenu = menu;
        this.isVisible = isVisible;
    }

    @Override
    public boolean isActive() {
        return !toolboxMenu.renderPass && isVisible;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && container.canPlaceItem(getContainerSlot(), stack);
    }
}
