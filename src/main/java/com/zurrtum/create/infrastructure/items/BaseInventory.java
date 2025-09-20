package com.zurrtum.create.infrastructure.items;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface BaseInventory extends Clearable {
    Hash.Strategy<ItemStack> ITEM_STACK_HASH_STRATEGY = new Hash.Strategy<>() {
        public boolean equals(ItemStack stack, ItemStack other) {
            return stack == other || stack != null && other != null && ItemStack.areItemsAndComponentsEqual(stack, other);
        }

        public int hashCode(ItemStack stack) {
            return ItemStack.hashCode(stack);
        }
    };

    @Override
    default void clear() {
        for (int i = 0, size = create$size(); i < size; i++) {
            create$setStack(i, ItemStack.EMPTY);
        }
        create$markDirty();
    }

    default int count(ItemStack stack, Direction side) {
        return count(stack);
    }

    default int count(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount);
    }

    default int count(ItemStack stack, int maxAmount, Direction side) {
        return count(stack, maxAmount);
    }

    default int count(ItemStack stack, int maxAmount) {
        int count = 0;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
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
        return count;
    }

    default ItemStack count(Predicate<ItemStack> predicate, Direction side) {
        return count(predicate);
    }

    default ItemStack count(Predicate<ItemStack> predicate) {
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack stack = create$getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                return onExtract(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        return count(predicate, maxAmount);
    }

    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack findStack = create$getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = create$getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
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

    default int countAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        return countAll(predicate, maxAmount);
    }

    default int countAll(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        int count = 0;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack stack = create$getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                count += stack.getCount();
                if (count >= maxAmount) {
                    return maxAmount;
                }
            }
        }
        return count;
    }

    default ItemStack countAny(Direction side) {
        return countAny();
    }

    default ItemStack countAny() {
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            return onExtract(directCopy(target, target.getCount()));
        }
        return ItemStack.EMPTY;
    }

    default ItemStack countAny(int maxAmount, Direction side) {
        return extractAny(maxAmount);
    }

    default ItemStack countAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack findStack = create$getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count >= maxAmount) {
                return onExtract(directCopy(findStack, maxAmount));
            }
            for (i = i + 1; i < size; i++) {
                ItemStack stack = create$getStack(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    count += stack.getCount();
                    if (count < maxAmount) {
                        continue;
                    }
                    return onExtract(directCopy(findStack, maxAmount));
                }
            }
            return onExtract(directCopy(findStack, count));
        }
        return ItemStack.EMPTY;
    }

    default int countSpace(ItemStack stack, Direction side) {
        return countSpace(stack);
    }

    default int countSpace(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount);
    }

    default int countSpace(ItemStack stack, int maxAmount, Direction side) {
        return countSpace(stack, maxAmount);
    }

    default int countSpace(ItemStack stack, int maxAmount) {
        int count = 0;
        for (int i = 0, size = create$size(); i < size; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    count += create$getMaxCount(stack);
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxCount() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    default int countSpace(ItemStack stack, int maxAmount, int start, int end, Direction side) {
        return countSpace(stack, maxAmount, start, end);
    }

    default int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    count += create$getMaxCount(stack);
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxCount() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    default boolean countSpace(List<ItemStack> stacks, Direction side) {
        return countSpace(stacks);
    }

    default boolean countSpace(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count) == count;
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
            return countSpace(stack, count) == count;
        }
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, create$getMaxCount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxCount();
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

    default boolean countSpace(List<ItemStack> stacks, int start, int end, Direction side) {
        return countSpace(stacks, start, end);
    }

    default boolean countSpace(List<ItemStack> stacks, int start, int end) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, start, end) == count;
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
            return countSpace(stack, count, start, end) == count;
        }
        for (int i = start; i <= end; i++) {
            ItemStack target = create$getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, create$getMaxCount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxCount();
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

    @SuppressWarnings("deprecation")
    default ItemStack directCopy(ItemStack stack, int count) {
        assert stack.item != null;
        ItemStack copy = new ItemStack(stack.item, count, stack.components.copy());
        copy.setBobbingAnimationTime(stack.getBobbingAnimationTime());
        return copy;
    }

    default int extract(ItemStack stack, Direction side) {
        return extract(stack);
    }

    default int extract(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount);
    }

    default int extract(ItemStack stack, int maxAmount, Direction side) {
        return extract(stack, maxAmount);
    }

    default int extract(ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                int count = target.getCount();
                if (count > remaining) {
                    target.setCount(count - remaining);
                    create$markDirty();
                    return maxAmount;
                }
                create$setStack(i, ItemStack.EMPTY);
                if (count == remaining) {
                    create$markDirty();
                    return maxAmount;
                }
                remaining -= count;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        create$markDirty();
        return maxAmount - remaining;
    }

    default ItemStack extract(Predicate<ItemStack> predicate, Direction side) {
        return extract(predicate);
    }

    default ItemStack extract(Predicate<ItemStack> predicate) {
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (predicate.test(target)) {
                create$setStack(i, ItemStack.EMPTY);
                create$markDirty();
                return onExtract(target);
            }
        }
        return ItemStack.EMPTY;
    }

    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        return extract(predicate, maxAmount);
    }

    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack findStack = create$getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    create$markDirty();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                create$setStack(i, ItemStack.EMPTY);
                if (count == maxAmount) {
                    create$markDirty();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = create$getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            create$setStack(i, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            create$setStack(i, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        create$markDirty();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                create$markDirty();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    default List<ItemStack> extract(List<ItemStack> stacks, Direction side) {
        return extract(stacks);
    }

    default List<ItemStack> extract(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int extract = extract(stack);
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
            int extract = extract(stack, count);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        boolean dirty = false;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (matches(target, stack)) {
                    int count = target.getCount();
                    int remaining = entry.getIntValue();
                    if (count < remaining) {
                        create$setStack(i, ItemStack.EMPTY);
                        entry.setValue(remaining - count);
                        break;
                    }
                    if (count == remaining) {
                        create$setStack(i, ItemStack.EMPTY);
                    } else {
                        target.setCount(count - remaining);
                    }
                    iterator.remove();
                    if (entries.isEmpty()) {
                        create$markDirty();
                        return List.of();
                    }
                    dirty = true;
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
            create$markDirty();
            return result;
        } else {
            return stacks;
        }
    }

    default int extractAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        return extractAll(predicate, maxAmount);
    }

    default int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        int remaining = maxAmount;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack stack = create$getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                int count = stack.getCount();
                if (count < remaining) {
                    create$setStack(i, ItemStack.EMPTY);
                    remaining -= count;
                    continue;
                }
                if (count == remaining) {
                    create$setStack(i, ItemStack.EMPTY);
                } else {
                    stack.setCount(count - remaining);
                }
                create$markDirty();
                return maxAmount;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        create$markDirty();
        return maxAmount - remaining;
    }

    default ItemStack extractAny(Direction side) {
        return extractAny();
    }

    default ItemStack extractAny() {
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            create$setStack(i, ItemStack.EMPTY);
            create$markDirty();
            return onExtract(target);
        }
        return ItemStack.EMPTY;
    }

    default ItemStack extractAny(int maxAmount, Direction side) {
        return extractAny(maxAmount);
    }

    default ItemStack extractAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack findStack = create$getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count > maxAmount) {
                findStack.setCount(count - maxAmount);
                create$markDirty();
                return onExtract(directCopy(findStack, maxAmount));
            }
            create$setStack(i, ItemStack.EMPTY);
            if (count == maxAmount) {
                create$markDirty();
                return onExtract(findStack);
            }
            int remaining = maxAmount - count;
            for (i = i + 1; i < size; i++) {
                ItemStack stack = create$getStack(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    count = stack.getCount();
                    if (count < remaining) {
                        create$setStack(i, ItemStack.EMPTY);
                        remaining -= count;
                        continue;
                    }
                    if (count == remaining) {
                        create$setStack(i, ItemStack.EMPTY);
                    } else {
                        stack.setCount(count - remaining);
                    }
                    create$markDirty();
                    findStack.setCount(maxAmount);
                    return onExtract(findStack);
                }
            }
            create$markDirty();
            findStack.setCount(maxAmount - remaining);
            return onExtract(findStack);
        }
        return ItemStack.EMPTY;
    }

    default int forceInsert(ItemStack stack) {
        return BaseInventory.this.insert(stack);
    }

    default int forceInsert(ItemStack stack, int maxAmount) {
        return BaseInventory.this.insert(stack, maxAmount);
    }

    default boolean forcePreciseInsert(ItemStack stack) {
        return BaseInventory.this.preciseInsert(stack);
    }

    default boolean forcePreciseInsert(ItemStack stack, int maxAmount) {
        return BaseInventory.this.preciseInsert(stack, maxAmount);
    }

    default int insert(ItemStack stack, Direction side) {
        return insert(stack);
    }

    default int insert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount);
    }

    default int insert(ItemStack stack, int maxAmount, Direction side) {
        return insert(stack, maxAmount);
    }

    default int insert(ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = create$size(); i < size; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, create$getMaxCount(stack));
                    create$setStack(i, directCopy(stack, insert));
                    if (remaining == insert) {
                        create$markDirty();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxCount();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            create$markDirty();
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
        create$markDirty();
        return maxAmount - remaining;
    }

    default int insert(ItemStack stack, int maxAmount, int start, int end, Direction side) {
        return insert(stack, maxAmount, start, end);
    }

    default int insert(ItemStack stack, int maxAmount, int start, int end) {
        int remaining = maxAmount;
        for (int i = start; i < end; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, create$getMaxCount(stack));
                    create$setStack(i, directCopy(stack, insert));
                    if (remaining == insert) {
                        create$markDirty();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxCount();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            create$markDirty();
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
        create$markDirty();
        return maxAmount - remaining;
    }

    default List<ItemStack> insert(List<ItemStack> stacks, Direction side) {
        return insert(stacks);
    }

    default List<ItemStack> insert(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack);
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
            int insert = insert(stack, count);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, create$getMaxCount(stack));
                        create$setStack(i, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                create$markDirty();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxCount();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    create$markDirty();
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
            create$markDirty();
            return result;
        } else {
            return stacks;
        }
    }

    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end, Direction side) {
        return insert(stacks, start, end);
    }

    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, count, start, end);
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
            int insert = insert(stack, count, start, end);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int i = start; i < end; i++) {
            ItemStack target = create$getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, create$getMaxCount(stack));
                        create$setStack(i, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                create$markDirty();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxCount();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    create$markDirty();
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
            create$markDirty();
            return result;
        } else {
            return stacks;
        }
    }

    default int insertExist(ItemStack stack, Direction side) {
        return insertExist(stack);
    }

    default int insertExist(ItemStack stack, int maxAmount, Direction side) {
        return insertExist(stack);
    }

    default int insertExist(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insertExist(stack, maxAmount);
    }

    default int insertExist(ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        List<Integer> emptys = new ArrayList<>();
        for (int i = 0, size = create$size(); i < size; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    emptys.add(i);
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxCount();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            create$markDirty();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        for (int i : emptys) {
            int insert = Math.min(remaining, create$getMaxCount(stack));
            create$setStack(i, directCopy(stack, insert));
            if (remaining == insert) {
                create$markDirty();
                return maxAmount;
            }
            remaining -= insert;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        create$markDirty();
        return maxAmount - remaining;
    }

    @NotNull
    default Iterator<ItemStack> iterator(Direction side) {
        return create$iterator();
    }

    default boolean matches(ItemStack stack, ItemStack otherStack) {
        if (stack.isOf(otherStack.getItem())) {
            MergedComponentMap stackComponents = stack.components;
            MergedComponentMap otherStackComponents = otherStack.components;
            if (stackComponents == otherStackComponents) {
                return true;
            }
            Reference2ObjectMap<ComponentType<?>, Optional<?>> stackComponentMap = stackComponents.changedComponents;
            Reference2ObjectMap<ComponentType<?>, Optional<?>> otherStackComponentMap = otherStackComponents.changedComponents;
            if (stackComponentMap == otherStackComponentMap) {
                return true;
            }
            int stackComponentCount = stackComponentMap.size();
            if (stackComponentMap.containsKey(DataComponentTypes.MAX_STACK_SIZE)) {
                stackComponentCount--;
            }
            int otherStackComponentCount = otherStackComponentMap.size();
            boolean hasMaxCapacityComponent = false;
            if (otherStackComponentMap.containsKey(DataComponentTypes.MAX_STACK_SIZE)) {
                otherStackComponentCount--;
                hasMaxCapacityComponent = true;
            }
            if (stackComponentCount != otherStackComponentCount) {
                return false;
            }
            if (hasMaxCapacityComponent) {
                ObjectSet<Reference2ObjectMap.Entry<ComponentType<?>, Optional<?>>> stackComponentSet = stackComponentMap.reference2ObjectEntrySet();
                for (Reference2ObjectMap.Entry<ComponentType<?>, Optional<?>> componentEntry : otherStackComponentMap.reference2ObjectEntrySet()) {
                    if (!stackComponentSet.contains(componentEntry) && componentEntry.getKey() != DataComponentTypes.MAX_STACK_SIZE) {
                        return false;
                    }
                }
                return true;
            }
            return stackComponentMap.reference2ObjectEntrySet().containsAll(otherStackComponentMap.reference2ObjectEntrySet());
        }
        return false;
    }

    default ItemStack onExtract(ItemStack stack) {
        return stack;
    }

    default boolean preciseExtract(ItemStack stack, Direction side) {
        return preciseExtract(stack);
    }

    default boolean preciseExtract(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                int count = target.getCount();
                if (count > remaining) {
                    changes.forEach(Runnable::run);
                    target.setCount(count - remaining);
                    create$markDirty();
                    return true;
                }
                if (count == remaining) {
                    changes.forEach(Runnable::run);
                    create$setStack(i, ItemStack.EMPTY);
                    create$markDirty();
                    return true;
                }
                int slot = i;
                changes.add(() -> create$setStack(slot, ItemStack.EMPTY));
                remaining -= count;
            }
        }
        return false;
    }

    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
        return preciseExtract(predicate, maxAmount);
    }

    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int size = create$size();
        List<Integer> buffer = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack findStack = create$getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    create$markDirty();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                if (count == maxAmount) {
                    create$setStack(i, ItemStack.EMPTY);
                    create$markDirty();
                    return onExtract(findStack);
                }
                buffer.add(i);
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = create$getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            buffer.add(i);
                            remaining -= count;
                            continue;
                        }
                        buffer.forEach(slot -> create$setStack(slot, ItemStack.EMPTY));
                        if (count == remaining) {
                            create$setStack(i, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        create$markDirty();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    default boolean preciseInsert(ItemStack stack, Direction side) {
        return preciseInsert(stack);
    }

    default boolean preciseInsert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(ItemStack stack, int maxAmount, Direction side) {
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(ItemStack stack, int maxAmount) {
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = create$size(); i < size; i++) {
            if (create$isValid(i, stack)) {
                ItemStack target = create$getStack(i);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, create$getMaxCount(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        create$setStack(i, directCopy(stack, insert));
                        create$markDirty();
                        return true;
                    }
                    int slot = i;
                    changes.add(() -> create$setStack(slot, directCopy(stack, insert)));
                    maxAmount -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxCount();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(maxAmount, maxCount - count);
                        if (maxAmount == insert) {
                            changes.forEach(Runnable::run);
                            target.setCount(count + insert);
                            create$markDirty();
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

    default boolean preciseInsert(List<ItemStack> stacks, Direction side) {
        return preciseInsert(stacks);
    }

    default boolean preciseInsert(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst());
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue());
        }
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack target = create$getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (create$isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, create$getMaxCount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                create$setStack(i, directCopy(stack, insert));
                                create$markDirty();
                                return true;
                            }
                        } else {
                            int slot = i;
                            changes.add(() -> create$setStack(slot, directCopy(stack, insert)));
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxCount = target.getMaxCount();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    changes.forEach(Runnable::run);
                                    target.setCount(count + insert);
                                    create$markDirty();
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

    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update, Direction side) {
        return update(predicate, update);
    }

    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        for (int i = 0, size = create$size(); i < size; i++) {
            ItemStack stack = create$getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                ItemStack replace = update.apply(stack);
                if (replace != stack) {
                    create$setStack(i, replace);
                }
                create$markDirty();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default ItemStack removeMaxSize(ItemStack stack, Optional<Integer> max) {
        stack.components.changedComponents.remove(DataComponentTypes.MAX_STACK_SIZE, max);
        return stack;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default void setMaxSize(ItemStack stack, Optional<Integer> max) {
        MergedComponentMap components = stack.components;
        components.onWrite();
        components.changedComponents.put(DataComponentTypes.MAX_STACK_SIZE, max);
    }

    default Stream<ItemStack> stream(Direction side) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(side), Spliterator.ORDERED), false);
    }

    default Stream<ItemStack> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(create$iterator(), Spliterator.ORDERED), false);
    }

    default void create$setStack(int slot, ItemStack stack) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int create$size() {
        throw new RuntimeException("Implemented via Mixin");
    }

    default int create$getMaxCount(ItemStack stack) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default ItemStack create$getStack(int slot) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default boolean create$isValid(int slot, ItemStack stack) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default Iterator<ItemStack> create$iterator() {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void create$markDirty() {
        throw new RuntimeException("Implemented via Mixin");
    }
}
