package com.zurrtum.create.content.schematics.cannon;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SchematicannonMenu extends MenuBase<SchematicannonBlockEntity> {

    public SchematicannonMenu(int id, PlayerInventory inv, SchematicannonBlockEntity be) {
        super(AllMenuTypes.SCHEMATICANNON, id, inv, be);
    }

    @Override
    protected void initAndReadInventory(SchematicannonBlockEntity contentHolder) {
    }

    @Override
    protected void addSlots() {
        int x = 0;
        int y = 0;

        addSlot(new MenuSlot(contentHolder.inventory, 0, x + 15, y + 65));
        addSlot(new MenuSlot(contentHolder.inventory, 1, x + 171, y + 65));
        addSlot(new MenuSlot(contentHolder.inventory, 2, x + 134, y + 19));
        addSlot(new MenuSlot(contentHolder.inventory, 3, x + 174, y + 19));
        addSlot(new MenuSlot(contentHolder.inventory, 4, x + 15, y + 19));

        addPlayerSlots(37, 161);
    }

    @Override
    protected void saveData(SchematicannonBlockEntity contentHolder) {
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasStack())
            return ItemStack.EMPTY;
        ItemStack stack = clickedSlot.getStack();

        if (index < 5) {
            insertItem(stack, 5, slots.size(), true);
        } else {
            if (insertItem(stack, 0, 1, false) || insertItem(stack, 2, 3, false) || insertItem(stack, 4, 5, false))
                ;
        }

        return ItemStack.EMPTY;
    }

    private static class MenuSlot extends Slot {
        public MenuSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public ItemStack takeStack(int amount) {
            return super.takeStack(Math.min(inventory.getMaxCount(getStack()), amount));
        }
    }
}
