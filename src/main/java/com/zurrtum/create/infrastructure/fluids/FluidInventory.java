package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.AllDataComponents;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.Direction;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.Clearable;

public interface FluidInventory extends Clearable, Iterable<FluidStack> {
    Hash.Strategy<FluidStack> FLUID_STACK_HASH_STRATEGY = new Hash.Strategy<>() {
        public boolean equals(FluidStack stack, FluidStack other) {
            return stack == other || stack != null && other != null && FluidStack.areFluidsAndComponentsEqual(stack, other);
        }

        public int hashCode(FluidStack stack) {
            return FluidStack.hashCode(stack);
        }
    };

    @Override
    default void clearContent() {
        for (int i = 0, size = size(); i < size; i++) {
            setStack(i, FluidStack.EMPTY);
        }
        markDirty();
    }

    default int count(FluidStack stack, Direction side) {
        return count(stack);
    }

    default int count(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount);
    }

    default int count(FluidStack stack, int maxAmount, Direction side) {
        return count(stack, maxAmount);
    }

    default int count(FluidStack stack, int maxAmount) {
        int amount = 0;
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                amount += target.getAmount();
                if (amount >= maxAmount) {
                    return maxAmount;
                }
            }
        }
        return amount;
    }

    default FluidStack count(Predicate<FluidStack> predicate, Direction side) {
        return count(predicate);
    }

    default FluidStack count(Predicate<FluidStack> predicate) {
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack stack = getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                return onExtract(stack);
            }
        }
        return FluidStack.EMPTY;
    }

    default FluidStack count(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        return count(predicate, maxAmount);
    }

    default FluidStack count(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount >= maxAmount) {
                    return onExtract(findStack.directCopy(maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    FluidStack stack = getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        amount += stack.getAmount();
                        if (amount < maxAmount) {
                            continue;
                        }
                        return onExtract(findStack.directCopy(maxAmount));
                    }
                }
                return onExtract(findStack.directCopy(amount));
            }
        }
        return FluidStack.EMPTY;
    }

    default FluidStack countAny(Direction side) {
        return countAny();
    }

    default FluidStack countAny() {
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            return onExtract(target.directCopy(target.getAmount()));
        }
        return FluidStack.EMPTY;
    }

    default FluidStack countAny(int maxAmount, Direction side) {
        return extractAny(maxAmount);
    }

    default FluidStack countAny(int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int amount = findStack.getAmount();
            if (amount >= maxAmount) {
                return onExtract(findStack.directCopy(maxAmount));
            }
            for (i = i + 1; i < size; i++) {
                FluidStack stack = getStack(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    amount += stack.getAmount();
                    if (amount < maxAmount) {
                        continue;
                    }
                    return onExtract(findStack.directCopy(maxAmount));
                }
            }
            return onExtract(findStack.directCopy(amount));
        }
        return FluidStack.EMPTY;
    }

    default int countSpace(FluidStack stack, Direction side) {
        return countSpace(stack);
    }

    default int countSpace(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount);
    }

    default int countSpace(FluidStack stack, int maxAmount, Direction side) {
        return countSpace(stack, maxAmount);
    }

    default int countSpace(FluidStack stack, int maxAmount) {
        int amount = 0;
        for (int i = 0, size = size(); i < size; i++) {
            if (isValid(i, stack)) {
                FluidStack target = getStack(i);
                if (target.isEmpty()) {
                    amount += getMaxAmount(stack);
                    if (amount >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    amount += target.getMaxAmount() - target.getAmount();
                    if (amount >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return amount;
    }

    default boolean countSpace(List<FluidStack> stacks, Direction side) {
        return countSpace(stacks);
    }

    default boolean countSpace(List<FluidStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            FluidStack stack = stacks.getFirst();
            int amount = stack.getAmount();
            return countSpace(stack, amount) == amount;
        }
        Object2IntLinkedOpenCustomHashMap<FluidStack> map = new Object2IntLinkedOpenCustomHashMap<>(FLUID_STACK_HASH_STRATEGY);
        for (FluidStack stack : stacks) {
            map.merge(stack, stack.getAmount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<FluidStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<FluidStack> entry = entries.first();
            FluidStack stack = entry.getKey();
            int amount = entry.getIntValue();
            return countSpace(stack, amount) == amount;
        }
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxAmount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxAmount = target.getMaxAmount();
                        int amount = target.getAmount();
                        if (amount != maxAmount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxAmount - amount);
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

    default int extract(FluidStack stack, Direction side) {
        return extract(stack);
    }

    default int extract(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount);
    }

    default int extract(FluidStack stack, int maxAmount, Direction side) {
        return extract(stack, maxAmount);
    }

    default int extract(FluidStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                int amount = target.getAmount();
                if (amount > remaining) {
                    target.setAmount(amount - remaining);
                    markDirty();
                    return maxAmount;
                }
                setStack(i, FluidStack.EMPTY);
                if (amount == remaining) {
                    markDirty();
                    return maxAmount;
                }
                remaining -= amount;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        markDirty();
        return maxAmount - remaining;
    }

    default FluidStack extract(Predicate<FluidStack> predicate, Direction side) {
        return extract(predicate);
    }

    default FluidStack extract(Predicate<FluidStack> predicate) {
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack stack = getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                setStack(i, FluidStack.EMPTY);
                markDirty();
                return onExtract(stack);
            }
        }
        return FluidStack.EMPTY;
    }

    default FluidStack extract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        return extract(predicate, maxAmount);
    }

    default FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount > maxAmount) {
                    findStack.setAmount(amount - maxAmount);
                    markDirty();
                    return onExtract(findStack.directCopy(maxAmount));
                }
                setStack(i, FluidStack.EMPTY);
                if (amount == maxAmount) {
                    markDirty();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - amount;
                for (i = i + 1; i < size; i++) {
                    FluidStack stack = getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        amount = stack.getAmount();
                        if (amount < remaining) {
                            setStack(i, FluidStack.EMPTY);
                            remaining -= amount;
                            continue;
                        }
                        if (amount == remaining) {
                            setStack(i, FluidStack.EMPTY);
                        } else {
                            stack.setAmount(amount - remaining);
                        }
                        markDirty();
                        findStack.setAmount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                markDirty();
                findStack.setAmount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return FluidStack.EMPTY;
    }

    default FluidStack extractAny(Direction side) {
        return extractAny();
    }

    default FluidStack extractAny() {
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            setStack(i, FluidStack.EMPTY);
            markDirty();
            return onExtract(target);
        }
        return FluidStack.EMPTY;
    }

    default FluidStack extractAny(int maxAmount, Direction side) {
        return extractAny(maxAmount);
    }

    default FluidStack extractAny(int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int amount = findStack.getAmount();
            if (amount > maxAmount) {
                findStack.setAmount(amount - maxAmount);
                markDirty();
                return onExtract(findStack.directCopy(maxAmount));
            }
            setStack(i, FluidStack.EMPTY);
            if (amount == maxAmount) {
                markDirty();
                return onExtract(findStack);
            }
            int remaining = maxAmount - amount;
            for (i = i + 1; i < size; i++) {
                FluidStack stack = getStack(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    amount = stack.getAmount();
                    if (amount < remaining) {
                        setStack(i, FluidStack.EMPTY);
                        remaining -= amount;
                        continue;
                    }
                    if (amount == remaining) {
                        setStack(i, FluidStack.EMPTY);
                    } else {
                        stack.setAmount(amount - remaining);
                    }
                    markDirty();
                    findStack.setAmount(maxAmount);
                    return onExtract(findStack);
                }
            }
            markDirty();
            findStack.setAmount(maxAmount - remaining);
            return onExtract(findStack);
        }
        return FluidStack.EMPTY;
    }

    default int forceInsert(FluidStack stack) {
        return FluidInventory.this.insert(stack);
    }

    default int forceInsert(FluidStack stack, int maxAmount) {
        return FluidInventory.this.insert(stack, maxAmount);
    }

    default boolean forcePreciseInsert(FluidStack stack) {
        return FluidInventory.this.preciseInsert(stack);
    }

    default boolean forcePreciseInsert(FluidStack stack, int maxAmount) {
        return FluidInventory.this.preciseInsert(stack, maxAmount);
    }

    default int getMaxAmount(FluidStack stack) {
        return Math.min(getMaxAmountPerStack(), stack.getMaxAmount());
    }

    default int getMaxAmountPerStack() {
        return Integer.MAX_VALUE;
    }

    FluidStack getStack(int slot);

    default int insert(FluidStack stack, Direction side) {
        return insert(stack);
    }

    default int insert(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount);
    }

    default int insert(FluidStack stack, int maxAmount, Direction side) {
        return insert(stack, maxAmount);
    }

    default int insert(FluidStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = size(); i < size; i++) {
            if (isValid(i, stack)) {
                FluidStack target = getStack(i);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxAmount(stack));
                    setStack(i, stack.directCopy(insert));
                    if (remaining == insert) {
                        markDirty();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxAmount();
                    int amount = target.getAmount();
                    if (amount != maxCount) {
                        int insert = Math.min(remaining, maxCount - amount);
                        target.setAmount(amount + insert);
                        if (remaining == insert) {
                            markDirty();
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
        markDirty();
        return maxAmount - remaining;
    }

    default List<FluidStack> insert(List<FluidStack> stacks, Direction side) {
        return insert(stacks);
    }

    default List<FluidStack> insert(List<FluidStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            FluidStack stack = stacks.getFirst();
            int amount = stack.getAmount();
            int insert = insert(stack);
            if (amount == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(stack.directCopy(amount - insert));
        }
        Object2IntLinkedOpenCustomHashMap<FluidStack> map = new Object2IntLinkedOpenCustomHashMap<>(FLUID_STACK_HASH_STRATEGY);
        for (FluidStack stack : stacks) {
            map.merge(stack, stack.getAmount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<FluidStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<FluidStack> entry = entries.first();
            FluidStack stack = entry.getKey();
            int amount = entry.getIntValue();
            int insert = insert(stack, amount);
            if (amount == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(stack.directCopy(amount - insert));
        }
        boolean dirty = false;
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            boolean empty = target.isEmpty();
            java.util.Iterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxAmount(stack));
                        setStack(i, stack.directCopy(insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                markDirty();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    } else if (matches(target, stack)) {
                        int maxAmount = target.getMaxAmount();
                        int amount = target.getAmount();
                        if (amount != maxAmount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxAmount - amount);
                            target.setAmount(amount + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    markDirty();
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
            List<FluidStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<FluidStack> entry : entries) {
                FluidStack stack = entry.getKey();
                int amount = entry.getIntValue();
                if (stack.getAmount() == amount) {
                    result.add(stack);
                } else {
                    result.add(stack.directCopy(amount));
                }
            }
            markDirty();
            return result;
        } else {
            return stacks;
        }
    }

    default int insertExist(FluidStack stack, Direction side) {
        return insertExist(stack);
    }

    default int insertExist(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int maxAmount = stack.getAmount();
        int remaining = maxAmount;
        List<Integer> emptys = new ArrayList<>();
        for (int i = 0, size = size(); i < size; i++) {
            if (isValid(i, stack)) {
                FluidStack target = getStack(i);
                if (target.isEmpty()) {
                    emptys.add(i);
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxAmount();
                    int amount = target.getAmount();
                    if (amount != maxCount) {
                        int insert = Math.min(remaining, maxCount - amount);
                        target.setAmount(amount + insert);
                        if (remaining == insert) {
                            markDirty();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        for (int i : emptys) {
            int insert = Math.min(remaining, getMaxAmount(stack));
            setStack(i, stack.directCopy(insert));
            if (remaining == insert) {
                markDirty();
                return maxAmount;
            }
            remaining -= insert;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        markDirty();
        return maxAmount - remaining;
    }

    default boolean isEmpty() {
        for (int i = 0, size = size(); i < size; i++) {
            if (!getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    default boolean isValid(int slot, FluidStack stack) {
        return true;
    }

    @NotNull
    default java.util.Iterator<FluidStack> iterator(Direction side) {
        return iterator();
    }

    @NotNull
    default java.util.Iterator<FluidStack> iterator() {
        return new Iterator(this);
    }

    default void markDirty() {
    }

    default boolean matches(FluidStack stack, FluidStack otherStack) {
        return FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(stack, otherStack);
    }

    default FluidStack onExtract(FluidStack stack) {
        return stack;
    }

    default boolean preciseExtract(FluidStack stack, Direction side) {
        return preciseExtract(stack);
    }

    default boolean preciseExtract(FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getAmount();
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                int amount = target.getAmount();
                if (amount > remaining) {
                    changes.forEach(Runnable::run);
                    target.setAmount(amount - remaining);
                    markDirty();
                    return true;
                }
                if (amount == remaining) {
                    changes.forEach(Runnable::run);
                    setStack(i, FluidStack.EMPTY);
                    markDirty();
                    return true;
                }
                final int slot = i;
                changes.add(() -> setStack(slot, FluidStack.EMPTY));
                remaining -= amount;
            }
        }
        return false;
    }

    default FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        return preciseExtract(predicate, maxAmount);
    }

    default FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        int size = size();
        List<Integer> buffer = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount > maxAmount) {
                    findStack.setAmount(amount - maxAmount);
                    markDirty();
                    return onExtract(findStack.directCopy(maxAmount));
                }
                if (amount == maxAmount) {
                    setStack(i, FluidStack.EMPTY);
                    markDirty();
                    return onExtract(findStack);
                }
                buffer.add(i);
                int remaining = maxAmount - amount;
                for (i = i + 1; i < size; i++) {
                    FluidStack stack = getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        amount = stack.getAmount();
                        if (amount < remaining) {
                            buffer.add(i);
                            remaining -= amount;
                            continue;
                        }
                        buffer.forEach(slot -> setStack(slot, FluidStack.EMPTY));
                        if (amount == remaining) {
                            setStack(i, FluidStack.EMPTY);
                        } else {
                            stack.setAmount(amount - remaining);
                        }
                        markDirty();
                        findStack.setAmount(maxAmount);
                        return onExtract(findStack);
                    }
                }
            }
        }
        return FluidStack.EMPTY;
    }

    default boolean preciseInsert(FluidStack stack, Direction side) {
        return preciseInsert(stack);
    }

    default boolean preciseInsert(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(FluidStack stack, int maxAmount, Direction side) {
        return preciseInsert(stack, maxAmount);
    }

    default boolean preciseInsert(FluidStack stack, int maxAmount) {
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = size(); i < size; i++) {
            if (isValid(i, stack)) {
                FluidStack target = getStack(i);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, getMaxAmount(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        setStack(i, stack.directCopy(insert));
                        markDirty();
                        return true;
                    }
                    int slot = i;
                    changes.add(() -> setStack(slot, stack.directCopy(insert)));
                    maxAmount -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxAmount();
                    int amount = target.getAmount();
                    if (amount != maxCount) {
                        int insert = Math.min(maxAmount, maxCount - amount);
                        if (maxAmount == insert) {
                            changes.forEach(Runnable::run);
                            target.setAmount(amount + insert);
                            markDirty();
                            return true;
                        }
                        changes.add(() -> target.setAmount(amount + insert));
                        maxAmount -= insert;
                    }
                }
            }
        }
        return false;
    }

    default boolean preciseInsert(List<FluidStack> stacks, Direction side) {
        return preciseInsert(stacks);
    }

    default boolean preciseInsert(List<FluidStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst());
        }
        Object2IntLinkedOpenCustomHashMap<FluidStack> map = new Object2IntLinkedOpenCustomHashMap<>(FLUID_STACK_HASH_STRATEGY);
        for (FluidStack stack : stacks) {
            map.merge(stack, stack.getAmount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<FluidStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<FluidStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue());
        }
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = size(); i < size; i++) {
            FluidStack target = getStack(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (isValid(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxAmount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                setStack(i, stack.directCopy(insert));
                                markDirty();
                                return true;
                            }
                        } else {
                            int slot = i;
                            changes.add(() -> setStack(slot, stack.directCopy(insert)));
                            entry.setValue(remaining - insert);
                        }
                    } else if (matches(target, stack)) {
                        int maxAmount = target.getMaxAmount();
                        int amount = target.getAmount();
                        if (amount != maxAmount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxAmount - amount);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    changes.forEach(Runnable::run);
                                    target.setAmount(amount + insert);
                                    markDirty();
                                    return true;
                                }
                            } else {
                                changes.add(() -> target.setAmount(amount + insert));
                                entry.setValue(remaining - insert);
                            }
                        }
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default FluidStack removeMaxSize(FluidStack stack, Optional<Integer> max) {
        PatchedDataComponentMap components = stack.directComponents();
        components.ensureMapOwnership();
        components.patch.remove(AllDataComponents.FLUID_MAX_CAPACITY, max);
        return stack;
    }

    default FluidStack removeStack(int slot, int amount) {
        if (slot >= size() || amount <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = getStack(slot);
        int count = stack.getAmount();
        if (count == 0) {
            return stack;
        }
        if (amount >= count) {
            setStack(slot, FluidStack.EMPTY);
            return onExtract(stack);
        }
        stack.setAmount(count - amount);
        return onExtract(stack.directCopy(amount));
    }

    default FluidStack removeStack(int slot) {
        if (slot >= size()) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = getStack(slot);
        if (stack.isEmpty()) {
            return stack;
        }
        setStack(slot, FluidStack.EMPTY);
        return onExtract(stack);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default void setMaxSize(FluidStack stack, Optional<Integer> max) {
        PatchedDataComponentMap components = stack.directComponents();
        components.ensureMapOwnership();
        components.patch.put(AllDataComponents.FLUID_MAX_CAPACITY, max);
    }

    void setStack(int slot, FluidStack stack);

    int size();

    default Stream<FluidStack> stream(Direction side) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(side), Spliterator.ORDERED), false);
    }

    default Stream<FluidStack> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    class Iterator implements java.util.Iterator<FluidStack> {
        private final FluidInventory inventory;
        private final int size;
        private int index;

        public Iterator(FluidInventory inventory) {
            this.inventory = inventory;
            this.size = inventory.size();
        }

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public FluidStack next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                return inventory.getStack(index++);
            }
        }
    }
}
