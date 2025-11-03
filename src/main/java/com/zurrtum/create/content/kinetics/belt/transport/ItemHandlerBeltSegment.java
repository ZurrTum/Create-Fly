package com.zurrtum.create.content.kinetics.belt.transport;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.item.ItemStack;

public class ItemHandlerBeltSegment implements ItemInventory {
    private final BeltInventory beltInventory;
    private final int offset;

    public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
        this.beltInventory = beltInventory;
        this.offset = offset;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return beltInventory.canInsertAt(offset);
    }

    @Override
    public boolean isEmpty() {
        TransportedItemStack transported = beltInventory.getStackAtOffset(offset);
        return transported == null || transported.stack.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot != 0)
            return ItemStack.EMPTY;
        TransportedItemStack stackAtOffset = beltInventory.getStackAtOffset(offset);
        if (stackAtOffset == null)
            return ItemStack.EMPTY;
        return stackAtOffset.stack;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot != 0)
            return ItemStack.EMPTY;
        TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
        if (transported == null)
            return ItemStack.EMPTY;

        amount = Math.min(amount, transported.stack.getCount());
        ItemStack extracted = transported.stack.split(amount);
        if (transported.stack.isEmpty())
            beltInventory.toRemove.add(transported);
        else
            markDirty();
        return extracted;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot != 0)
            return ItemStack.EMPTY;
        TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
        if (transported == null)
            return ItemStack.EMPTY;
        beltInventory.toRemove.add(transported);
        ItemStack stack = transported.stack;
        transported.stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        if (stack.isEmpty()) {
            TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
            if (transported == null || transported.stack.isEmpty())
                return;
            beltInventory.toRemove.add(transported);
            transported.stack = stack;
        } else {
            TransportedItemStack newStack = new TransportedItemStack(stack);
            newStack.insertedAt = offset;
            newStack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
            newStack.prevBeltPosition = newStack.beltPosition;
            beltInventory.addItem(newStack);
        }
    }

    @Override
    public void markDirty() {
        beltInventory.belt.notifyUpdate();
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return Math.min(stack.getMaxCount(), 64);
    }
}
