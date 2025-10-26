package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class PackagePortMenu extends MenuBase<PackagePortBlockEntity> {
    public PackagePortMenu(int id, PlayerInventory inv, PackagePortBlockEntity be) {
        super(AllMenuTypes.PACKAGE_PORT, id, inv, be);
        BlockEntityBehaviour.get(be, AnimatedContainerBehaviour.TYPE).startOpen(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        // based on the impl from chests.
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        // we need to copy the stack here since it may be modified by moveItemStackTo, but the
        // stack may be taken directly from a SlotItemHandler, which just defers to an IItemHandler.
        // modifying the original stack would violate the class's contract and cause problems.
        ItemStack stack = slot.getStack().copy();
        // we return the stack that was moved out of the slot, so make a copy of that now too.
        ItemStack moved = stack.copy();

        int size = contentHolder.inventory.size();
        if (index < size) {
            // move into player inventory
            if (!insertItem(stack, size, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // move into port inventory
            if (!insertItem(stack, 0, size, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            // setByPlayer instead of just setChanged, since we made a copy
            // setByPlayer instead of set because, I don't know, that's what the other branch does
            slot.setStack(stack.copy());
        }

        return moved;
    }

    @Override
    protected void initAndReadInventory(PackagePortBlockEntity contentHolder) {
    }

    @Override
    protected void addSlots() {
        Inventory inventory = contentHolder.inventory;
        int x = 27;
        int y = 9;

        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, row * 9 + col, x + col * 18, y + row * 18));

        addPlayerSlots(38, 108);
    }

    @Override
    protected void saveData(PackagePortBlockEntity contentHolder) {
    }

    @Override
    public void onClosed(PlayerEntity playerIn) {
        super.onClosed(playerIn);
        if (!playerIn.getWorld().isClient)
            BlockEntityBehaviour.get(contentHolder, AnimatedContainerBehaviour.TYPE).stopOpen(playerIn);
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        // unfortunately, we kinda need to copy this entire method to make two tiny changes. I'm surprised
        // there's no forge patch for this considering it violates the contract of IItemHandler.getStackInSlot.

        boolean success = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack stackInSlot = slot.getStack();
                if (!stackInSlot.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, stackInSlot)) {
                    int totalCount = stackInSlot.getCount() + stack.getCount();
                    // note: forge patches this variable in, vanilla just uses stack.getMaxStackSize 4 times
                    int maxSize = Math.min(slot.getMaxItemCount(), stack.getMaxCount());
                    if (totalCount <= maxSize) {
                        stack.setCount(0);
                        // change #1: set a new stack instead of modifying it directly
                        slot.setStack(stackInSlot.copyWithCount(totalCount));
                        success = true;
                    } else if (stackInSlot.getCount() < maxSize) {
                        stack.decrement(maxSize - stackInSlot.getCount());
                        // change #2: set a new stack instead of modifying it directly
                        slot.setStack(stackInSlot.copyWithCount(maxSize));
                        success = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot.isEmpty() && slot.canInsert(stack)) {
                    if (stack.getCount() > slot.getMaxItemCount()) {
                        slot.setStack(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }

                    slot.markDirty();
                    success = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return success;
    }
}
