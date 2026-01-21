package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.NoSuchElementException;

public class CombinedTankWrapper implements SidedFluidInventory {
    protected final FluidInventory[] itemHandler;
    protected final int[] baseIndex;
    private final boolean[] sideInventory;
    protected final int[] slots;

    public CombinedTankWrapper(FluidInventory... itemHandler) {
        this.itemHandler = itemHandler;
        int length = itemHandler.length;
        this.baseIndex = new int[length];
        this.sideInventory = new boolean[length];
        int index = 0;
        for (int i = 0; i < length; i++) {
            FluidInventory inventory = itemHandler[i];
            index += inventory.size();
            baseIndex[i] = index;
            sideInventory[i] = inventory instanceof SidedFluidInventory;
        }
        this.slots = SlotRangeCache.get(index);
    }

    protected int getIndexForSlot(int slot) {
        if (slot < 0)
            return -1;

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    protected FluidInventory getHandlerFromIndex(int index) {
        if (index < 0 || index >= itemHandler.length) {
            return null;
        }
        return itemHandler[index];
    }

    protected int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= baseIndex.length) {
            return slot;
        }
        return slot - baseIndex[index - 1];
    }

    @Override
    public int[] getAvailableSlots(@Nullable Direction side) {
        return slots;
    }

    @Override
    public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
        int index = getIndexForSlot(slot);
        FluidInventory handler = getHandlerFromIndex(index);
        if (handler == null) {
            return false;
        }
        if (handler.isValid(slot, stack)) {
            if (sideInventory[index]) {
                return ((SidedFluidInventory) handler).canInsert(slot, stack, dir);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, FluidStack stack, Direction dir) {
        int index = getIndexForSlot(slot);
        FluidInventory handler = getHandlerFromIndex(index);
        if (handler == null) {
            return false;
        }
        if (sideInventory[index]) {
            return ((SidedFluidInventory) handler).canExtract(slot, stack, dir);
        }
        return true;
    }

    @Override
    public int size() {
        return slots.length;
    }

    @Override
    public FluidStack getStack(int slot) {
        int index = getIndexForSlot(slot);
        FluidInventory handler = getHandlerFromIndex(index);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        slot = getSlotFromIndex(slot, index);
        return handler.getStack(slot);
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        int index = getIndexForSlot(slot);
        FluidInventory handler = getHandlerFromIndex(index);
        if (handler == null) {
            return;
        }
        slot = getSlotFromIndex(slot, index);
        handler.setStack(slot, stack);
    }

    @Override
    public int insert(FluidStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (FluidInventory handler : itemHandler) {
            int insert = handler.insert(stack, remaining, side);
            if (remaining == insert) {
                markInventoryDirty();
                return maxAmount;
            }
            if (insert == 0) {
                continue;
            }
            remaining -= insert;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        markInventoryDirty();
        return maxAmount - remaining;
    }

    @Override
    public int extract(FluidStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (FluidInventory handler : itemHandler) {
            int extract = handler.extract(stack, remaining, side);
            if (remaining == extract) {
                markInventoryDirty();
                return maxAmount;
            }
            if (extract == 0) {
                continue;
            }
            remaining -= extract;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        markInventoryDirty();
        return maxAmount - remaining;
    }

    public void markInventoryDirty() {
    }

    @Override
    public void markDirty() {
        for (FluidInventory inventory : itemHandler) {
            inventory.markDirty();
        }
        markInventoryDirty();
    }

    @Override
    public java.util.Iterator<FluidStack> iterator(Direction side) {
        if (itemHandler.length == 0) {
            return Collections.emptyIterator();
        } else {
            return new com.zurrtum.create.infrastructure.fluids.CombinedTankWrapper.Iterator(side);
        }
    }

    class Iterator implements java.util.Iterator<FluidStack> {
        private int index;
        private final Direction side;
        private java.util.Iterator<FluidStack> iterator;

        public Iterator(Direction side) {
            this.side = side;
            this.iterator = itemHandler[index].iterator(side);
        }

        @Override
        public boolean hasNext() {
            if (iterator.hasNext()) {
                return true;
            }
            do {
                index++;
                if (index >= itemHandler.length) {
                    return false;
                }
                iterator = itemHandler[index].iterator(side);
            } while (!iterator.hasNext());
            return true;
        }

        @Override
        public FluidStack next() {
            if (hasNext()) {
                return iterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
