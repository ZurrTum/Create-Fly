package com.zurrtum.create.infrastructure.items;

import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class CombinedInvWrapper implements SidedItemInventory {
    protected final Container[] itemHandler;
    protected final int[] baseIndex;
    private final boolean[] sideInventory;
    protected final int[] slots;

    public CombinedInvWrapper(Container... itemHandler) {
        this.itemHandler = itemHandler;
        int length = itemHandler.length;
        this.baseIndex = new int[length];
        this.sideInventory = new boolean[length];
        int index = 0;
        for (int i = 0; i < length; i++) {
            Container inventory = itemHandler[i];
            index += inventory.getContainerSize();
            baseIndex[i] = index;
            sideInventory[i] = inventory instanceof WorldlyContainer;
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
    protected Container getHandlerFromIndex(int index) {
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
    public int[] getSlotsForFace(@Nullable Direction side) {
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        int index = getIndexForSlot(slot);
        Container handler = getHandlerFromIndex(index);
        if (handler == null) {
            return false;
        }
        if (handler.canPlaceItem(slot, stack)) {
            if (sideInventory[index]) {
                return ((WorldlyContainer) handler).canPlaceItemThroughFace(slot, stack, dir);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        int index = getIndexForSlot(slot);
        Container handler = getHandlerFromIndex(index);
        if (handler == null) {
            return false;
        }
        if (sideInventory[index]) {
            return ((WorldlyContainer) handler).canTakeItemThroughFace(slot, stack, dir);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return slots.length;
    }

    @Override
    public ItemStack getItem(int slot) {
        int index = getIndexForSlot(slot);
        Container handler = getHandlerFromIndex(index);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        slot = getSlotFromIndex(slot, index);
        return handler.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int index = getIndexForSlot(slot);
        Container handler = getHandlerFromIndex(index);
        if (handler == null) {
            return;
        }
        slot = getSlotFromIndex(slot, index);
        handler.setItem(slot, stack);
    }

    @Override
    public int insert(ItemStack stack, int maxAmount, @Nullable Direction side) {
        int remaining = maxAmount;
        for (Container handler : itemHandler) {
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
    public int extract(ItemStack stack, int maxAmount, @Nullable Direction side) {
        int remaining = maxAmount;
        for (Container handler : itemHandler) {
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

    @Override
    public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = itemHandler.length; i < size; i++) {
            ItemStack findStack = itemHandler[i].extract(predicate, maxAmount, side);
            if (findStack == ItemStack.EMPTY) {
                continue;
            }
            int extract = findStack.getCount();
            if (extract == maxAmount) {
                markInventoryDirty();
                return findStack;
            }
            i++;
            if (i == size) {
                markInventoryDirty();
                return findStack;
            }
            int remaining = maxAmount - extract;
            for (; i < size; i++) {
                extract = itemHandler[i].extract(findStack, remaining, side);
                if (remaining == extract) {
                    markInventoryDirty();
                    findStack.setCount(maxAmount);
                    return findStack;
                }
                if (extract == 0) {
                    continue;
                }
                remaining -= extract;
            }
            markInventoryDirty();
            findStack.setCount(maxAmount - remaining);
            return findStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = itemHandler.length; i < size; i++) {
            ItemStack findStack = itemHandler[i].count(predicate, maxAmount, side);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count == maxAmount) {
                itemHandler[i].extract(findStack, count, side);
                markInventoryDirty();
                return findStack;
            }
            i++;
            if (i == size) {
                break;
            }
            int[] extracts = new int[size];
            extracts[i] = count;
            int remaining = maxAmount - count;
            for (; i < size; i++) {
                int extract = itemHandler[i].count(findStack, remaining, side);
                if (extract == 0) {
                    continue;
                }
                extracts[i] = extract;
                if (remaining > extract) {
                    remaining -= extract;
                    continue;
                }
                for (int j = 0; j <= i; j++) {
                    extract = extracts[j];
                    if (extract == 0) {
                        continue;
                    }
                    itemHandler[j].extract(findStack, extract, side);
                }
                markInventoryDirty();
                findStack.setCount(maxAmount);
                return findStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int countAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int count = 0;
        for (Container inventory : itemHandler) {
            count += inventory.countAll(predicate, maxAmount, side);
            if (count >= maxAmount) {
                return maxAmount;
            }
        }
        return count;
    }

    @Override
    public int extractAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int remaining = maxAmount;
        for (Container inventory : itemHandler) {
            int extract = inventory.extractAll(predicate, remaining, side);
            if (extract < remaining) {
                remaining -= extract;
                continue;
            }
            markInventoryDirty();
            return maxAmount;
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
    public void setChanged() {
        for (Container inventory : itemHandler) {
            inventory.setChanged();
        }
        markInventoryDirty();
    }

    @Override
    public java.util.Iterator<ItemStack> iterator(@Nullable Direction side) {
        if (itemHandler.length == 0) {
            return Collections.emptyIterator();
        } else {
            return new com.zurrtum.create.infrastructure.items.CombinedInvWrapper.Iterator(side);
        }
    }

    class Iterator implements java.util.Iterator<ItemStack> {
        private int index;
        private final @Nullable Direction side;
        private java.util.Iterator<ItemStack> iterator;

        public Iterator(@Nullable Direction side) {
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
        public ItemStack next() {
            if (hasNext()) {
                return iterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
