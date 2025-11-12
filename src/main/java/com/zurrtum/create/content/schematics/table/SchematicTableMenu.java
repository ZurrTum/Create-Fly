package com.zurrtum.create.content.schematics.table;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SchematicTableMenu extends MenuBase<SchematicTableBlockEntity> {

    private Slot inputSlot;
    private Slot outputSlot;

    public SchematicTableMenu(int id, Inventory inv, SchematicTableBlockEntity be) {
        super(AllMenuTypes.SCHEMATIC_TABLE, id, inv, be);
    }

    public boolean canWrite() {
        return inputSlot.hasItem() && !outputSlot.hasItem();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getItem();
        if (index < 2)
            moveItemStackTo(stack, 2, slots.size(), true);
        else
            moveItemStackTo(stack, 0, 1, false);

        return ItemStack.EMPTY;
    }

    @Override
    protected void initAndReadInventory(SchematicTableBlockEntity contentHolder) {
    }

    @Override
    protected void addSlots() {
        inputSlot = new Slot(contentHolder.inventory, 0, 21, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(AllItems.EMPTY_SCHEMATIC) || stack.is(AllItems.SCHEMATIC_AND_QUILL) || stack.is(AllItems.SCHEMATIC);
            }
        };

        outputSlot = new Slot(contentHolder.inventory, 1, 166, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };

        addSlot(inputSlot);
        addSlot(outputSlot);

        // player Slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 38 + col * 18, 107 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new Slot(player.getInventory(), hotbarSlot, 38 + hotbarSlot * 18, 165));
        }
    }

    @Override
    protected void saveData(SchematicTableBlockEntity contentHolder) {
    }

}