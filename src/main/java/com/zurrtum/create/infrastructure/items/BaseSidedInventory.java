package com.zurrtum.create.infrastructure.items;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface BaseSidedInventory extends Container {
    default int[] create$getAvailableSlots(Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean create$canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        throw new RuntimeException("Implemented via Mixin");
    }


    default boolean create$canExtract(int slot, ItemStack stack, Direction dir) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int count(ItemStack stack) {
        return count(stack, null);
    }

    @Override
    default int count(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount, side);
    }

    @Override
    default int count(ItemStack stack, int maxAmount) {
        return count(stack, maxAmount, null);
    }

    @Override
    default int count(ItemStack stack, int maxAmount, Direction side) {
        int count = 0;
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canExtract(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    count += target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate) {
        return count(predicate, null);
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, Direction side) {
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, stack, side) && predicate.test(stack)) {
                return onExtract(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
        return count(predicate, maxAmount, null);
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = create$getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (create$canExtract(slot, stack, side) && matches(stack, findStack)) {
                        count += stack.getCount();
                        if (count < maxAmount) {
                            continue;
                        }
                        return onExtract(directCopy(findStack, maxAmount));
                    }
                }
                return onExtract(directCopy(findStack, count));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default int countAll(Predicate<ItemStack> predicate, int maxAmount) {
        return countAll(predicate, maxAmount, null);
    }

    @Override
    default int countAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int count = 0;
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && create$canExtract(slot, stack, side)) {
                count += stack.getCount();
                if (count >= maxAmount) {
                    return maxAmount;
                }
            }
        }
        return count;
    }

    @Override
    default ItemStack countAny() {
        return countAny(null);
    }

    @Override
    default ItemStack countAny(Direction side) {
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, target, side)) {
                return onExtract(directCopy(target, target.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack countAny(int maxAmount) {
        return extractAny(maxAmount, null);
    }

    @Override
    default ItemStack countAny(int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = create$getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, findStack, side)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (create$canExtract(slot, stack, side) && matches(stack, findStack)) {
                        count += stack.getCount();
                        if (count < maxAmount) {
                            continue;
                        }
                        return onExtract(directCopy(findStack, maxAmount));
                    }
                }
                return onExtract(directCopy(findStack, count));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default int countSpace(ItemStack stack) {
        return countSpace(stack, null);
    }

    @Override
    default int countSpace(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount, side);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount) {
        return countSpace(stack, maxAmount, null);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, Direction side) {
        int count = 0;
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack) - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxStackSize() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        return countSpace(stack, maxAmount, start, end, null);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, int start, int end, Direction side) {
        int count = 0;
        int[] slots = create$getAvailableSlots(side);
        start = findStartIndex(slots, start);
        if (start == -1) {
            return 0;
        }
        end = findEndIndex(slots, start, end);
        if (end == -1) {
            return 0;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack) - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxStackSize() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks) {
        return countSpace(stacks, null);
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, side) == count;
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            return countSpace(stack, count, side) == count;
        }
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    return true;
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                        }
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, int start, int end) {
        return countSpace(stacks, start, end, null);
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, int start, int end, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, start, end, side) == count;
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            return countSpace(stack, count, start, end, side) == count;
        }
        int[] slots = create$getAvailableSlots(side);
        start = findStartIndex(slots, start);
        if (start == -1) {
            return false;
        }
        end = findEndIndex(slots, start, end);
        if (end == -1) {
            return false;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    return true;
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                        }
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default int extract(ItemStack stack) {
        return extract(stack, null);
    }

    @Override
    default int extract(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount, side);
    }

    @Override
    default int extract(ItemStack stack, int maxAmount) {
        return extract(stack, maxAmount, null);
    }

    @Override
    default int extract(ItemStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canExtract(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    int count = target.getCount();
                    if (count > remaining) {
                        target.setCount(count - remaining);
                        setChanged();
                        return maxAmount;
                    }
                    setItem(slot, ItemStack.EMPTY);
                    if (count == remaining) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= count;
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        return extract(predicate, maxAmount, null);
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = create$getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (create$canExtract(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate) {
        return extract(predicate, null);
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, Direction side) {
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, stack, side) && predicate.test(stack)) {
                setItem(slot, ItemStack.EMPTY);
                setChanged();
                return onExtract(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default List<ItemStack> extract(List<ItemStack> stacks) {
        return extract(stacks, null);
    }

    @Override
    default List<ItemStack> extract(List<ItemStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int extract = extract(stack, side);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int extract = extract(stack, count, side);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        boolean dirty = false;
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, target, side)) {
                ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                do {
                    Object2IntMap.Entry<ItemStack> entry = iterator.next();
                    ItemStack stack = entry.getKey();
                    if (matches(target, stack)) {
                        int count = target.getCount();
                        int remaining = entry.getIntValue();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            entry.setValue(remaining - count);
                            break;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            target.setCount(count - remaining);
                        }
                        iterator.remove();
                        if (entries.isEmpty()) {
                            setChanged();
                            return List.of();
                        }
                        dirty = true;
                    }
                } while (iterator.hasNext());
            }
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        } else {
            return stacks;
        }
    }

    @Override
    default int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        return extractAll(predicate, maxAmount, null);
    }

    @Override
    default int extractAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int remaining = maxAmount;
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && create$canExtract(slot, stack, side)) {
                int count = stack.getCount();
                if (count < remaining) {
                    setItem(slot, ItemStack.EMPTY);
                    remaining -= count;
                    continue;
                }
                if (count == remaining) {
                    setItem(slot, ItemStack.EMPTY);
                } else {
                    stack.setCount(count - remaining);
                }
                setChanged();
                return maxAmount;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default ItemStack extractAny() {
        return extractAny(null);
    }

    @Override
    default ItemStack extractAny(Direction side) {
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, target, side)) {
                setItem(slot, ItemStack.EMPTY);
                setChanged();
                return onExtract(target);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack extractAny(int maxAmount) {
        return extractAny(maxAmount, null);
    }

    @Override
    default ItemStack extractAny(int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = create$getAvailableSlots(side);
        int size = slots.length;
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, findStack, side)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (create$canExtract(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    private int findEndIndex(int[] slots, int start, int end) {
        for (int i = slots.length - 1; i >= start; i--) {
            if (slots[i] <= end) {
                return i;
            }
        }
        return -1;
    }

    private int findStartIndex(int[] slots, int start) {
        for (int i = 0, size = slots.length; i < size; i++) {
            if (slots[i] >= start) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default int insert(ItemStack stack) {
        return insert(stack, null);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount) {
        return insert(stack, maxAmount, null);
    }

    @Override
    default int insert(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount, side);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(slot, directCopy(stack, insert));
                    if (remaining == insert) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, int start, int end) {
        return insert(stack, maxAmount, start, end, null);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, int start, int end, Direction side) {
        int remaining = maxAmount;
        int[] slots = create$getAvailableSlots(side);
        start = findStartIndex(slots, start);
        if (start == -1) {
            return 0;
        }
        end = findEndIndex(slots, start, end);
        if (end == -1) {
            return 0;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(slot, directCopy(stack, insert));
                    if (remaining == insert) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks) {
        return insert(stacks, null);
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int insert = insert(stack, count, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(slot, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                setChanged();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    setChanged();
                                    return List.of();
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                            dirty = true;
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        } else {
            return stacks;
        }
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        return insert(stacks, start, end, null);
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, count, start, end, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int insert = insert(stack, count, start, end, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        int[] slots = create$getAvailableSlots(side);
        start = findStartIndex(slots, start);
        if (start == -1) {
            return stacks;
        }
        end = findEndIndex(slots, start, end);
        if (end == -1) {
            return stacks;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(slot, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                setChanged();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    setChanged();
                                    return List.of();
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                            dirty = true;
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        } else {
            return stacks;
        }
    }

    @Override
    default int insertExist(ItemStack stack) {
        return insertExist(stack, null);
    }

    @Override
    default int insertExist(ItemStack stack, int maxAmount) {
        return insertExist(stack, maxAmount, null);
    }

    @Override
    default int insertExist(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insertExist(stack, maxAmount, side);
    }

    @Override
    default int insertExist(ItemStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        List<Integer> emptys = new ArrayList<>();
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    emptys.add(slot);
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        for (int slot : emptys) {
            int insert = Math.min(remaining, getMaxStackSize(stack));
            setItem(slot, directCopy(stack, insert));
            if (remaining == insert) {
                setChanged();
                return maxAmount;
            }
            remaining -= insert;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    @NotNull
    default java.util.Iterator<ItemStack> iterator() {
        return iterator(null);
    }

    @Override
    @NotNull
    default java.util.Iterator<ItemStack> iterator(Direction side) {
        return new Iterator(this, side);
    }

    @Override
    default boolean preciseExtract(ItemStack stack) {
        return preciseExtract(stack, null);
    }

    @Override
    default boolean preciseExtract(ItemStack stack, Direction side) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        List<Runnable> changes = new ArrayList<>();
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canExtract(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    int count = target.getCount();
                    if (count > remaining) {
                        changes.forEach(Runnable::run);
                        target.setCount(count - remaining);
                        setChanged();
                        return true;
                    }
                    if (count == remaining) {
                        changes.forEach(Runnable::run);
                        setItem(slot, ItemStack.EMPTY);
                        setChanged();
                        return true;
                    }
                    changes.add(() -> setItem(slot, ItemStack.EMPTY));
                    remaining -= count;
                }
            }
        }
        return false;
    }

    @Override
    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        return preciseExtract(predicate, maxAmount, null);
    }

    @Override
    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = create$getAvailableSlots(side);
        int size = slots.length;
        List<Integer> buffer = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (create$canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                if (count == maxAmount) {
                    setItem(slot, ItemStack.EMPTY);
                    setChanged();
                    return onExtract(findStack);
                }
                buffer.add(slot);
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (create$canExtract(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            buffer.add(slot);
                            remaining -= count;
                            continue;
                        }
                        buffer.forEach(j -> setItem(j, ItemStack.EMPTY));
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default boolean preciseInsert(ItemStack stack) {
        return preciseInsert(stack, null);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount, side);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount, null);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, int maxAmount, Direction side) {
        List<Runnable> changes = new ArrayList<>();
        for (int slot : create$getAvailableSlots(side)) {
            if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, getMaxStackSize(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        setItem(slot, directCopy(stack, insert));
                        setChanged();
                        return true;
                    }
                    changes.add(() -> setItem(slot, directCopy(stack, insert)));
                    maxAmount -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(maxAmount, maxCount - count);
                        if (maxAmount == insert) {
                            changes.forEach(Runnable::run);
                            target.setCount(count + insert);
                            setChanged();
                            return true;
                        }
                        changes.add(() -> target.setCount(count + insert));
                        maxAmount -= insert;
                    }
                }
            }
        }
        return false;
    }

    @Override
    default boolean preciseInsert(List<ItemStack> stacks) {
        return preciseInsert(stacks, null);
    }

    @Override
    default boolean preciseInsert(List<ItemStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst(), side);
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue(), side);
        }
        List<Runnable> changes = new ArrayList<>();
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$canInsert(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                setItem(slot, directCopy(stack, insert));
                                setChanged();
                                return true;
                            }
                        } else {
                            changes.add(() -> setItem(slot, directCopy(stack, insert)));
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    changes.forEach(Runnable::run);
                                    target.setCount(count + insert);
                                    setChanged();
                                    return true;
                                }
                            } else {
                                changes.add(() -> target.setCount(count + insert));
                                entry.setValue(remaining - insert);
                            }
                        }
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        return update(predicate, update, null);
    }

    @Override
    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update, Direction side) {
        for (int slot : create$getAvailableSlots(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && create$canExtract(slot, stack, side)) {
                ItemStack replace = update.apply(stack);
                if (replace != stack) {
                    setItem(slot, replace);
                }
                setChanged();
                return true;
            }
        }
        return false;
    }

    class Iterator implements java.util.Iterator<ItemStack> {
        private final BaseSidedInventory inventory;
        private final Direction side;
        private final int[] slots;
        private int index;
        private int current = -1;

        public Iterator(BaseSidedInventory inventory, Direction side) {
            this.inventory = inventory;
            this.side = side;
            this.slots = inventory.create$getAvailableSlots(side);
        }

        @Override
        public boolean hasNext() {
            if (current >= 0) {
                return true;
            }
            if (current == -2) {
                return false;
            }
            for (; index < slots.length; index++) {
                ItemStack stack = inventory.getItem(slots[index]);
                if (inventory.create$canExtract(index, stack, side)) {
                    current = index;
                    index++;
                    return true;
                }
            }
            current = -2;
            return false;
        }

        @Override
        public ItemStack next() {
            if (hasNext()) {
                ItemStack result = inventory.getItem(slots[current]);
                current = -1;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
