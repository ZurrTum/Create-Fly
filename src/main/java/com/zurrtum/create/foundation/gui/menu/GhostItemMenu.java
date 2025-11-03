package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public abstract class GhostItemMenu<T> extends MenuBase<T> implements IClearableMenu {

    public ItemStackHandler ghostInventory;

    protected GhostItemMenu(MenuType<T> type, int id, PlayerInventory inv, T contentHolder) {
        super(type, id, inv, contentHolder);
    }

    protected abstract ItemStackHandler createGhostInventory();

    protected abstract boolean allowRepeats();

    @Override
    protected void initAndReadInventory(T contentHolder) {
        ghostInventory = createGhostInventory();
    }

    @Override
    public void clearContents() {
        for (int i = 0, size = ghostInventory.size(); i < size; i++)
            ghostInventory.setStack(i, ItemStack.EMPTY);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slotIn) {
        return slotIn.inventory == playerInventory;
    }

    @Override
    public boolean canInsertIntoSlot(Slot slotIn) {
        if (allowRepeats())
            return true;
        return slotIn.inventory == playerInventory;
    }

    @Override
    public void onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        if (slotId < 36) {
            super.onSlotClick(slotId, dragType, clickTypeIn, player);
            return;
        }
        if (clickTypeIn == SlotActionType.THROW)
            return;

        ItemStack held = getCursorStack();
        int slot = slotId - 36;
        if (clickTypeIn == SlotActionType.CLONE) {
            if (player.isCreative() && held.isEmpty()) {
                ItemStack stackInSlot = ghostInventory.getStack(slot).copy();
                stackInSlot.setCount(stackInSlot.getMaxCount());
                setCursorStack(stackInSlot);
                return;
            }
            return;
        }

        ItemStack insert;
        if (held.isEmpty()) {
            insert = ItemStack.EMPTY;
        } else {
            insert = held.copy();
            insert.setCount(1);
        }
        ghostInventory.setStack(slot, insert);
        getSlot(slotId).markDirty();
    }

    @Override
    protected boolean insertItem(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerIn, int index) {
        if (index < 36) {
            Slot slot = slots.get(index);
            ItemStack stackToInsert = slot.getStack();
            for (int i = 0, size = ghostInventory.size(); i < size; i++) {
                ItemStack stack = ghostInventory.getStack(i);
                if (!allowRepeats() && ItemStack.areItemsAndComponentsEqual(stack, stackToInsert))
                    break;
                if (stack.isEmpty()) {
                    ItemStack copy = stackToInsert.copy();
                    copy.setCount(1);
                    ghostInventory.setStack(i, copy);
                    getSlot(i + 36).markDirty();
                    break;
                }
            }
        } else {
            int i = index - 36;
            ItemStack stack = ghostInventory.getStack(i);
            int count = stack.getCount();
            if (count == 1) {
                ghostInventory.setStack(i, ItemStack.EMPTY);
            } else if (count > 1) {
                stack.setCount(count - 1);
            }
            getSlot(index).markDirty();
        }
        return ItemStack.EMPTY;
    }

}