package com.zurrtum.create.content.schematics.table;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SchematicTableMenu extends MenuBase<SchematicTableBlockEntity> {

    private Slot inputSlot;
    private Slot outputSlot;

    public SchematicTableMenu(int id, PlayerInventory inv, SchematicTableBlockEntity be) {
        super(AllMenuTypes.SCHEMATIC_TABLE, id, inv, be);
    }

    public boolean canWrite() {
        return inputSlot.hasStack() && !outputSlot.hasStack();
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasStack())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getStack();
        if (index < 2)
            insertItem(stack, 2, slots.size(), true);
        else
            insertItem(stack, 0, 1, false);

        return ItemStack.EMPTY;
    }

    @Override
    protected void initAndReadInventory(SchematicTableBlockEntity contentHolder) {
    }

    @Override
    protected void addSlots() {
        inputSlot = new Slot(contentHolder.inventory, 0, 21, 59) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(AllItems.EMPTY_SCHEMATIC) || stack.isOf(AllItems.SCHEMATIC_AND_QUILL) || stack.isOf(AllItems.SCHEMATIC);
            }
        };

        outputSlot = new Slot(contentHolder.inventory, 1, 166, 59) {
            @Override
            public boolean canInsert(ItemStack stack) {
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