package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class GhostItemMenu<T> extends MenuBase<T> implements IClearableMenu {

    public ItemStackHandler ghostInventory;

    protected GhostItemMenu(MenuType<T> type, int id, Inventory inv, T contentHolder) {
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
        for (int i = 0, size = ghostInventory.getContainerSize(); i < size; i++)
            ghostInventory.setItem(i, ItemStack.EMPTY);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        return slotIn.container == playerInventory;
    }

    @Override
    public boolean canDragTo(Slot slotIn) {
        if (allowRepeats())
            return true;
        return slotIn.container == playerInventory;
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId < 36) {
            super.clicked(slotId, dragType, clickTypeIn, player);
            return;
        }
        if (clickTypeIn == ContainerInput.THROW)
            return;

        ItemStack held = getCarried();
        int slot = slotId - 36;
        if (clickTypeIn == ContainerInput.CLONE) {
            if (player.isCreative() && held.isEmpty()) {
                ItemStack stackInSlot = ghostInventory.getItem(slot).copy();
                stackInSlot.setCount(stackInSlot.getMaxStackSize());
                setCarried(stackInSlot);
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
        ghostInventory.setItem(slot, insert);
        getSlot(slotId).setChanged();
    }

    @Override
    protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (index < 36) {
            Slot slot = slots.get(index);
            ItemStack stackToInsert = slot.getItem();
            for (int i = 0, size = ghostInventory.getContainerSize(); i < size; i++) {
                ItemStack stack = ghostInventory.getItem(i);
                if (!allowRepeats() && ItemStack.isSameItemSameComponents(stack, stackToInsert))
                    break;
                if (stack.isEmpty()) {
                    ItemStack copy = stackToInsert.copy();
                    copy.setCount(1);
                    ghostInventory.setItem(i, copy);
                    getSlot(i + 36).setChanged();
                    break;
                }
            }
        } else {
            int i = index - 36;
            ItemStack stack = ghostInventory.getItem(i);
            int count = stack.getCount();
            if (count == 1) {
                ghostInventory.setItem(i, ItemStack.EMPTY);
            } else if (count > 1) {
                stack.setCount(count - 1);
            }
            getSlot(index).setChanged();
        }
        return ItemStack.EMPTY;
    }

}