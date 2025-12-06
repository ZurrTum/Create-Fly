package com.zurrtum.create.infrastructure.fluids;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public interface SidedFluidInventory extends FluidInventory {
    boolean canExtract(int slot, FluidStack stack, Direction dir);

    boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir);

    @Override
    default int count(FluidStack stack) {
        return count(stack, null);
    }

    @Override
    default int count(FluidStack stack, Direction side) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount, side);
    }

    @Override
    default int count(FluidStack stack, int maxAmount) {
        return count(stack, maxAmount, null);
    }

    @Override
    default int count(FluidStack stack, int maxAmount, Direction side) {
        int amount = 0;
        for (int slot : getAvailableSlots(side)) {
            if (canExtract(slot, stack, side)) {
                FluidStack target = getStack(slot);
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
        }
        return amount;
    }

    @Override
    default FluidStack count(Predicate<FluidStack> predicate) {
        return count(predicate, null);
    }

    @Override
    default FluidStack count(Predicate<FluidStack> predicate, Direction side) {
        for (int slot : getAvailableSlots(side)) {
            FluidStack stack = getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, stack, side) && predicate.test(stack)) {
                return onExtract(stack);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    default FluidStack count(Predicate<FluidStack> predicate, int maxAmount) {
        return count(predicate, maxAmount, null);
    }

    @Override
    default FluidStack count(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        int[] slots = getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            FluidStack findStack = getStack(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount >= maxAmount) {
                    return onExtract(findStack.directCopy(maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    FluidStack stack = getStack(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canExtract(slot, stack, side) && matches(stack, findStack)) {
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

    @Override
    default FluidStack countAny() {
        return countAny(null);
    }

    @Override
    default FluidStack countAny(Direction side) {
        for (int slot : getAvailableSlots(side)) {
            FluidStack target = getStack(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (canExtract(slot, target, side)) {
                return onExtract(target.directCopy(target.getAmount()));
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    default FluidStack countAny(int maxAmount) {
        return extractAny(maxAmount, null);
    }

    @Override
    default FluidStack countAny(int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        int[] slots = getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            FluidStack findStack = getStack(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, findStack, side)) {
                int amount = findStack.getAmount();
                if (amount >= maxAmount) {
                    return onExtract(findStack.directCopy(maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    FluidStack stack = getStack(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canExtract(slot, stack, side) && matches(stack, findStack)) {
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

    @Override
    default int countSpace(FluidStack stack) {
        return countSpace(stack, null);
    }

    @Override
    default int countSpace(FluidStack stack, Direction side) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount, side);
    }

    @Override
    default int countSpace(FluidStack stack, int maxAmount) {
        return countSpace(stack, maxAmount, null);
    }

    @Override
    default int countSpace(FluidStack stack, int maxAmount, Direction side) {
        int amount = 0;
        for (int slot : getAvailableSlots(side)) {
            if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                FluidStack target = getStack(slot);
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

    @Override
    default boolean countSpace(List<FluidStack> stacks) {
        return countSpace(stacks, null);
    }

    @Override
    default boolean countSpace(List<FluidStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            FluidStack stack = stacks.getFirst();
            int amount = stack.getAmount();
            return countSpace(stack, amount, side) == amount;
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
            return countSpace(stack, amount, side) == amount;
        }
        for (int slot : getAvailableSlots(side)) {
            FluidStack target = getStack(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (canInsert(slot, stack, side) && isValid(slot, stack)) {
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
                        break;
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
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default int extract(FluidStack stack) {
        return extract(stack, null);
    }

    @Override
    default int extract(FluidStack stack, Direction side) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount, side);
    }

    @Override
    default int extract(FluidStack stack, int maxAmount) {
        return extract(stack, maxAmount, null);
    }

    @Override
    default int extract(FluidStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (int slot : getAvailableSlots(side)) {
            if (canExtract(slot, stack, side)) {
                FluidStack target = getStack(slot);
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
                    setStack(slot, FluidStack.EMPTY);
                    if (amount == remaining) {
                        markDirty();
                        return maxAmount;
                    }
                    remaining -= amount;
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        markDirty();
        return maxAmount - remaining;
    }

    @Override
    default FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
        return extract(predicate, maxAmount, null);
    }

    @Override
    default FluidStack extract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        int[] slots = getAvailableSlots(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            FluidStack findStack = getStack(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount > maxAmount) {
                    findStack.setAmount(amount - maxAmount);
                    markDirty();
                    return onExtract(findStack.directCopy(maxAmount));
                }
                setStack(slot, FluidStack.EMPTY);
                if (amount == maxAmount) {
                    markDirty();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - amount;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    FluidStack stack = getStack(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canExtract(slot, stack, side) && matches(stack, findStack)) {
                        amount = stack.getAmount();
                        if (amount < remaining) {
                            setStack(slot, FluidStack.EMPTY);
                            remaining -= amount;
                            continue;
                        }
                        if (amount == remaining) {
                            setStack(slot, FluidStack.EMPTY);
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

    @Override
    default FluidStack extract(Predicate<FluidStack> predicate) {
        return extract(predicate, null);
    }

    @Override
    default FluidStack extract(Predicate<FluidStack> predicate, Direction side) {
        for (int slot : getAvailableSlots(side)) {
            FluidStack stack = getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, stack, side) && predicate.test(stack)) {
                setStack(slot, FluidStack.EMPTY);
                markDirty();
                return onExtract(stack);
            }
        }
        return FluidStack.EMPTY;
    }

    int[] getAvailableSlots(@Nullable Direction side);

    @Override
    default int insert(FluidStack stack) {
        return insert(stack, null);
    }

    @Override
    default int insert(FluidStack stack, int maxAmount) {
        return insert(stack, maxAmount, null);
    }

    @Override
    default int insert(FluidStack stack, Direction side) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount, side);
    }

    @Override
    default int insert(FluidStack stack, int maxAmount, Direction side) {
        int remaining = maxAmount;
        for (int slot : getAvailableSlots(side)) {
            if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                FluidStack target = getStack(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxAmount(stack));
                    setStack(slot, stack.directCopy(insert));
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

    @Override
    default List<FluidStack> insert(List<FluidStack> stacks) {
        return insert(stacks, null);
    }

    @Override
    default List<FluidStack> insert(List<FluidStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            FluidStack stack = stacks.getFirst();
            int amount = stack.getAmount();
            int insert = insert(stack, side);
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
            int insert = insert(stack, amount, side);
            if (amount == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(stack.directCopy(amount - insert));
        }
        boolean dirty = false;
        for (int slot : getAvailableSlots(side)) {
            FluidStack target = getStack(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxAmount(stack));
                        setStack(slot, stack.directCopy(insert));
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

    @Override
    default int insertExist(FluidStack stack) {
        return insertExist(stack, null);
    }

    @Override
    default int insertExist(FluidStack stack, Direction side) {
        if (stack.isEmpty()) {
            return 0;
        }
        int maxAmount = stack.getAmount();
        int remaining = maxAmount;
        List<Integer> emptys = new ArrayList<>();
        for (int slot : getAvailableSlots(side)) {
            if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                FluidStack target = getStack(slot);
                if (target.isEmpty()) {
                    emptys.add(slot);
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
        for (int slot : emptys) {
            int insert = Math.min(remaining, getMaxAmount(stack));
            setStack(slot, stack.directCopy(insert));
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

    @Override
    @NotNull
    default java.util.Iterator<FluidStack> iterator() {
        return iterator(null);
    }

    @Override
    @NotNull
    default java.util.Iterator<FluidStack> iterator(Direction side) {
        return new Iterator(this, side);
    }

    @Override
    default boolean preciseExtract(FluidStack stack) {
        return preciseExtract(stack, null);
    }

    @Override
    default boolean preciseExtract(FluidStack stack, Direction side) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getAmount();
        List<Runnable> changes = new ArrayList<>();
        for (int slot : getAvailableSlots(side)) {
            if (canExtract(slot, stack, side)) {
                FluidStack target = getStack(slot);
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
                        setStack(slot, FluidStack.EMPTY);
                        markDirty();
                        return true;
                    }
                    changes.add(() -> setStack(slot, FluidStack.EMPTY));
                    remaining -= amount;
                }
            }
        }
        return false;
    }

    @Override
    default FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount) {
        return preciseExtract(predicate, maxAmount, null);
    }

    @Override
    default FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        int[] slots = getAvailableSlots(side);
        int size = slots.length;
        List<Integer> buffer = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            FluidStack findStack = getStack(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canExtract(slot, findStack, side) && predicate.test(findStack)) {
                int amount = findStack.getAmount();
                if (amount > maxAmount) {
                    findStack.setAmount(amount - maxAmount);
                    markDirty();
                    return onExtract(findStack.directCopy(maxAmount));
                }
                if (amount == maxAmount) {
                    setStack(slot, FluidStack.EMPTY);
                    markDirty();
                    return onExtract(findStack);
                }
                buffer.add(slot);
                int remaining = maxAmount - amount;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    FluidStack stack = getStack(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canExtract(slot, stack, side) && matches(stack, findStack)) {
                        amount = stack.getAmount();
                        if (amount < remaining) {
                            buffer.add(slot);
                            remaining -= amount;
                            continue;
                        }
                        buffer.forEach(j -> setStack(j, FluidStack.EMPTY));
                        if (amount == remaining) {
                            setStack(slot, FluidStack.EMPTY);
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

    @Override
    default boolean preciseInsert(FluidStack stack) {
        return preciseInsert(stack, null);
    }

    @Override
    default boolean preciseInsert(FluidStack stack, Direction side) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount, side);
    }

    @Override
    default boolean preciseInsert(FluidStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount, null);
    }

    @Override
    default boolean preciseInsert(FluidStack stack, int maxAmount, Direction side) {
        List<Runnable> changes = new ArrayList<>();
        for (int slot : getAvailableSlots(side)) {
            if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                FluidStack target = getStack(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, getMaxAmount(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        setStack(slot, stack.directCopy(insert));
                        markDirty();
                        return true;
                    }
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

    @Override
    default boolean preciseInsert(List<FluidStack> stacks) {
        return preciseInsert(stacks, null);
    }

    @Override
    default boolean preciseInsert(List<FluidStack> stacks, Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst(), side);
        }
        Object2IntLinkedOpenCustomHashMap<FluidStack> map = new Object2IntLinkedOpenCustomHashMap<>(FLUID_STACK_HASH_STRATEGY);
        for (FluidStack stack : stacks) {
            map.merge(stack, stack.getAmount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<FluidStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<FluidStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue(), side);
        }
        List<Runnable> changes = new ArrayList<>();
        for (int slot : getAvailableSlots(side)) {
            FluidStack target = getStack(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                if (canInsert(slot, stack, side) && isValid(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxAmount(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                setStack(slot, stack.directCopy(insert));
                                markDirty();
                                return true;
                            }
                        } else {
                            changes.add(() -> setStack(slot, stack.directCopy(insert)));
                            entry.setValue(remaining - insert);
                        }
                        break;
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
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    class Iterator implements java.util.Iterator<FluidStack> {
        private final SidedFluidInventory inventory;
        private final Direction side;
        private final int[] slots;
        private int index;
        private int current = -1;

        public Iterator(SidedFluidInventory inventory, Direction side) {
            this.inventory = inventory;
            this.side = side;
            this.slots = inventory.getAvailableSlots(side);
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
                FluidStack stack = inventory.getStack(slots[index]);
                if (inventory.canExtract(index, stack, side)) {
                    current = index;
                    index++;
                    return true;
                }
            }
            current = -2;
            return false;
        }

        @Override
        public FluidStack next() {
            if (hasNext()) {
                FluidStack result = inventory.getStack(slots[current]);
                current = -1;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
