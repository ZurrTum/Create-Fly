package com.zurrtum.create.content.schematics.cannon;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SchematicannonMenu extends MenuBase<SchematicannonBlockEntity> {

    public SchematicannonMenu(int id, Inventory inv, SchematicannonBlockEntity be) {
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
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;
        ItemStack stack = clickedSlot.getItem();

        if (index < 5) {
            moveItemStackTo(stack, 5, slots.size(), true);
        } else {
            if (moveItemStackTo(stack, 0, 1, false) || moveItemStackTo(stack, 2, 3, false) || moveItemStackTo(stack, 4, 5, false))
                ;
        }

        return ItemStack.EMPTY;
    }

    private static class MenuSlot extends Slot {
        public MenuSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public ItemStack remove(int amount) {
            return super.remove(Math.min(container.getMaxStackSize(getItem()), amount));
        }
    }
}
