package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class FluidInventoryWrapper<T extends Storage<FluidVariant>, S extends StorageView<FluidVariant>> implements FluidInventory {
    protected T storage;
    protected int capacity;

    public FluidInventoryWrapper(T storage) {
        this.storage = storage;
        init();
    }

    public static FluidInventory of(Storage<FluidVariant> storage) {
        FluidInventoryWrapper<?, ?> inventory;
        if (storage instanceof SlottedStorage<FluidVariant> slottedStorage) {
            inventory = new Slotted(slottedStorage);
        } else {
            inventory = new Direct(storage);
        }
        if (storage.supportsInsertion() && storage.supportsExtraction()) {
            return inventory;
        }
        return new Filter(inventory);
    }

    @Override
    public void clearContent() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                view.extract(view.getResource(), view.getAmount(), transaction);
            }
            transaction.commit();
        }
    }

    @Override
    public int count(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount);
    }

    @Override
    public int count(FluidStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extract = storage.extract(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.abort();
            return (int) extract;
        }
    }

    @Override
    public FluidStack count(Predicate<FluidStack> predicate) {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                if (predicate.test(stack)) {
                    long extract = view.extract(variant, view.getAmount(), transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.abort();
                    return stack.directCopy((int) extract);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack count(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.abort();
                    return stack.directCopy((int) extract);
                }
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack countAny() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                long extract = view.extract(variant, view.getAmount(), transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.abort();
                return stack.directCopy((int) extract);
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack countAny(int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                long extract = storage.extract(variant, maxAmount, transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.abort();
                return stack.directCopy((int) extract);
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int countSpace(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount);
    }

    @Override
    public int countSpace(FluidStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.abort();
            return (int) insert;
        }
    }

    @Override
    public boolean countSpace(List<FluidStack> stacks) {
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
        try (Transaction transaction = Transaction.openOuter()) {
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                int amount = entry.getIntValue();
                long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), amount, transaction);
                if (insert < amount) {
                    transaction.abort();
                    return false;
                }
            } while (iterator.hasNext());
            transaction.abort();
            return true;
        }
    }

    @Override
    public int extract(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount);
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extract = storage.extract(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.commit();
            return (int) extract;
        }
    }

    @Override
    public FluidStack extract(Predicate<FluidStack> predicate) {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                if (predicate.test(stack)) {
                    long extract = view.extract(variant, view.getAmount(), transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.commit();
                    return stack.directCopy((int) extract);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.commit();
                    return stack.directCopy((int) extract);
                }
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack extractAny() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                long extract = view.extract(variant, view.getAmount(), transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.commit();
                return FluidInventoryStorage.getCachedStack(variant).directCopy((int) extract);
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack extractAny(int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                long extract = view.extract(variant, maxAmount, transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.commit();
                return FluidInventoryStorage.getCachedStack(variant).directCopy((int) extract);
            }
            transaction.abort();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int forceInsert(FluidStack stack) {
        return insert(stack);
    }

    @Override
    public int forceInsert(FluidStack stack, int maxAmount) {
        return insert(stack, maxAmount);
    }

    @Override
    public boolean forcePreciseInsert(FluidStack stack) {
        return preciseInsert(stack);
    }

    @Override
    public boolean forcePreciseInsert(FluidStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount);
    }

    @Override
    public int getMaxAmountPerStack() {
        return capacity;
    }

    protected abstract S getSlot(int slot);

    @Override
    public FluidStack getStack(int slot) {
        if (slot >= size()) {
            return FluidStack.EMPTY;
        }
        StorageView<FluidVariant> view = getSlot(slot);
        if (view == null) {
            return FluidStack.EMPTY;
        }
        FluidVariant variant = view.getResource();
        return new FluidStack(variant.getFluid(), view.getAmount(), variant.getComponents());
    }

    protected void init() {
        int max = 0;
        for (StorageView<FluidVariant> view : storage) {
            long capacity = view.getCapacity();
            if (max > capacity) {
                max = (int) capacity;
            }
        }
        capacity = max;
    }

    protected abstract void insert(S view, FluidVariant variant, int amount, Transaction transaction);

    @Override
    public int insert(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount);
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.commit();
            return (int) insert;
        }
    }

    @Override
    public List<FluidStack> insert(List<FluidStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            FluidStack stack = stacks.getFirst();
            int amount = stack.getAmount();
            int insert = insert(stacks.getFirst());
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
        try (Transaction transaction = Transaction.openOuter()) {
            boolean dirty = false;
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                int amount = entry.getIntValue();
                long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), amount, transaction);
                if (insert == amount) {
                    iterator.remove();
                    if (entries.isEmpty()) {
                        transaction.commit();
                        return List.of();
                    }
                    dirty = true;
                } else if (insert != 0) {
                    entry.setValue(amount - (int) insert);
                    dirty = true;
                }
            } while (iterator.hasNext());
            if (dirty) {
                List<FluidStack> result = new ArrayList<>();
                for (Object2IntMap.Entry<FluidStack> entry : entries) {
                    FluidStack stack = entry.getKey();
                    int amount = entry.getIntValue();
                    if (stack.getAmount() == amount) {
                        result.add(stack);
                    } else {
                        result.add(stack.directCopy(amount));
                    }
                }
                transaction.commit();
                return result;
            } else {
                return stacks;
            }
        }
    }

    @Override
    public int insertExist(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), stack.getAmount(), transaction);
            transaction.commit();
            return (int) insert;
        }
    }

    @Override
    public boolean isEmpty() {
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            return false;
        }
        return true;
    }

    @Override
    @NotNull
    public java.util.Iterator<FluidStack> iterator() {
        return storage.supportsExtraction() ? new com.zurrtum.create.infrastructure.transfer.FluidInventoryWrapper.Iterator(storage) : Collections.emptyIterator();
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            int amount = stack.getAmount();
            long extract = storage.extract(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), amount, transaction);
            if (extract < amount) {
                transaction.abort();
                return false;
            } else {
                transaction.commit();
                return true;
            }
        }
    }

    @Override
    public FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                FluidVariant variant = view.getResource();
                FluidStack stack = FluidInventoryStorage.getCachedStack(variant);
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    if (extract == maxAmount) {
                        transaction.commit();
                        return stack.directCopy(maxAmount);
                    } else {
                        transaction.abort();
                    }
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean preciseInsert(FluidStack stack) {
        int maxAmount = stack.getAmount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount);
    }

    @Override
    public boolean preciseInsert(FluidStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), maxAmount, transaction);
            if (insert < maxAmount) {
                transaction.abort();
                return false;
            } else {
                transaction.commit();
                return true;
            }
        }
    }

    @Override
    public boolean preciseInsert(List<FluidStack> stacks) {
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
        try (Transaction transaction = Transaction.openOuter()) {
            ObjectIterator<Object2IntMap.Entry<FluidStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<FluidStack> entry = iterator.next();
                FluidStack stack = entry.getKey();
                int amount = entry.getIntValue();
                long insert = storage.insert(FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), amount, transaction);
                if (insert < amount) {
                    transaction.abort();
                    return false;
                }
            } while (iterator.hasNext());
            transaction.commit();
            return true;
        }
    }

    @Override
    public FluidStack removeStack(int slot) {
        if (slot >= size()) {
            return FluidStack.EMPTY;
        }
        StorageView<FluidVariant> view = getSlot(slot);
        if (view == null) {
            return FluidStack.EMPTY;
        }
        FluidVariant variant = view.getResource();
        long amount = view.getAmount();
        if (variant.isBlank() || amount == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            amount = view.extract(variant, amount, transaction);
            transaction.commit();
        }
        return new FluidStack(variant.getFluid(), amount, variant.getComponents());
    }

    @Override
    public FluidStack removeStack(int slot, int amount) {
        if (slot >= size()) {
            return FluidStack.EMPTY;
        }
        StorageView<FluidVariant> view = getSlot(slot);
        if (view == null) {
            return FluidStack.EMPTY;
        }
        FluidVariant variant = view.getResource();
        if (variant.isBlank() || view.getAmount() == 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            amount = (int) view.extract(variant, amount, transaction);
            transaction.commit();
        }
        return new FluidStack(variant.getFluid(), amount, variant.getComponents());
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        if (slot >= size()) {
            return;
        }
        S view = getSlot(slot);
        if (view == null) {
            return;
        }
        FluidVariant variant = view.getResource();
        try (Transaction transaction = Transaction.openOuter()) {
            if (variant.isBlank() || view.getAmount() == 0) {
                if (stack.isEmpty()) {
                    return;
                }
                insert(view, FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), stack.getAmount(), transaction);
            } else if (FluidInventoryStorage.matches(variant, stack)) {
                int amount = stack.getAmount();
                int targetAmount = (int) view.getAmount();
                if (amount == targetAmount) {
                    return;
                }
                int change = targetAmount - amount;
                if (amount < targetAmount) {
                    insert(view, variant, change, transaction);
                } else {
                    view.extract(variant, change, transaction);
                }
            } else {
                view.extract(variant, view.getAmount(), transaction);
                insert(view, FluidVariant.of(stack.getFluid(), stack.getComponentChanges()), stack.getAmount(), transaction);
            }
            transaction.commit();
        }
    }

    @Override
    public Stream<FluidStack> stream() {
        return storage.supportsExtraction() ? FluidInventory.super.stream() : Stream.empty();
    }

    public static class Filter implements SidedFluidInventory {
        private final int[] slots;
        private final boolean canInsert;
        private final boolean canExtract;
        private final FluidInventoryWrapper<?, ?> inventory;

        public Filter(FluidInventoryWrapper<?, ?> inventory) {
            this.inventory = inventory;
            this.slots = SlotRangeCache.get(inventory.size());
            this.canInsert = inventory.storage.supportsInsertion();
            this.canExtract = inventory.storage.supportsExtraction();
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, Direction dir) {
            return canExtract;
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
            return canInsert;
        }

        @Override
        public void clearContent() {
            if (!canExtract) {
                return;
            }
            inventory.clearContent();
        }

        @Override
        public int count(FluidStack stack, Direction side) {
            return count(stack);
        }

        @Override
        public int count(FluidStack stack) {
            if (!canExtract) {
                return 0;
            }
            return inventory.count(stack);
        }

        @Override
        public int count(FluidStack stack, int maxAmount, Direction side) {
            return count(stack, maxAmount);
        }

        @Override
        public int count(FluidStack stack, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.count(stack, maxAmount);
        }

        @Override
        public FluidStack count(Predicate<FluidStack> predicate, Direction side) {
            return count(predicate);
        }

        @Override
        public FluidStack count(Predicate<FluidStack> predicate) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.count(predicate);
        }

        @Override
        public FluidStack count(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
            return count(predicate, maxAmount);
        }

        @Override
        public FluidStack count(Predicate<FluidStack> predicate, int maxAmount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.count(predicate, maxAmount);
        }

        @Override
        public FluidStack countAny(Direction side) {
            return countAny();
        }

        @Override
        public FluidStack countAny() {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.countAny();
        }

        @Override
        public FluidStack countAny(int maxAmount, Direction side) {
            return countAny(maxAmount);
        }

        @Override
        public FluidStack countAny(int maxAmount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.countAny(maxAmount);
        }

        @Override
        public int countSpace(FluidStack stack, Direction side) {
            return countSpace(stack);
        }

        @Override
        public int countSpace(FluidStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.countSpace(stack);
        }

        @Override
        public int countSpace(FluidStack stack, int maxAmount, Direction side) {
            return countSpace(stack, maxAmount);
        }

        @Override
        public int countSpace(FluidStack stack, int maxAmount) {
            if (!canInsert) {
                return 0;
            }
            return inventory.countSpace(stack, maxAmount);
        }

        @Override
        public int extract(FluidStack stack, Direction side) {
            return extract(stack);
        }

        @Override
        public int extract(FluidStack stack) {
            if (!canExtract) {
                return 0;
            }
            return inventory.extract(stack);
        }

        @Override
        public int extract(FluidStack stack, int maxAmount, Direction side) {
            return extract(stack, maxAmount);
        }

        @Override
        public int extract(FluidStack stack, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.extract(stack, maxAmount);
        }

        @Override
        public FluidStack extract(Predicate<FluidStack> predicate, Direction side) {
            return extract(predicate);
        }

        @Override
        public FluidStack extract(Predicate<FluidStack> predicate) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.extract(predicate);
        }

        @Override
        public FluidStack extract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
            return extract(predicate, maxAmount);
        }

        @Override
        public FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.extract(predicate, maxAmount);
        }

        @Override
        public FluidStack extractAny(Direction side) {
            return extractAny();
        }

        @Override
        public FluidStack extractAny() {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.extractAny();
        }

        @Override
        public FluidStack extractAny(int maxAmount, Direction side) {
            return extractAny(maxAmount);
        }

        @Override
        public FluidStack extractAny(int maxAmount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.extractAny(maxAmount);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return slots;
        }

        @Override
        public int getMaxAmountPerStack() {
            return inventory.getMaxAmountPerStack();
        }

        @Override
        public FluidStack getStack(int slot) {
            return inventory.getStack(slot);
        }

        @Override
        public int insert(FluidStack stack, Direction side) {
            return insert(stack);
        }

        @Override
        public int insert(FluidStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insert(stack);
        }

        @Override
        public int insert(FluidStack stack, int maxAmount, Direction side) {
            return insert(stack, maxAmount);
        }

        @Override
        public int insert(FluidStack stack, int maxAmount) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insert(stack, maxAmount);
        }

        @Override
        public List<FluidStack> insert(List<FluidStack> stacks, Direction side) {
            return insert(stacks);
        }

        @Override
        public List<FluidStack> insert(List<FluidStack> stacks) {
            if (!canInsert) {
                return stacks;
            }
            return inventory.insert(stacks);
        }

        @Override
        public int insertExist(FluidStack stack, Direction side) {
            return insertExist(stack);
        }

        @Override
        public int insertExist(FluidStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insertExist(stack);
        }

        @Override
        public boolean isEmpty() {
            return inventory.isEmpty();
        }

        @Override
        public @NotNull java.util.Iterator<FluidStack> iterator() {
            return inventory.iterator();
        }

        @Override
        public @NotNull java.util.Iterator<FluidStack> iterator(Direction side) {
            return inventory.iterator();
        }

        @Override
        public boolean preciseExtract(FluidStack stack, Direction side) {
            return preciseExtract(stack);
        }

        @Override
        public boolean preciseExtract(FluidStack stack) {
            if (!canExtract) {
                return stack.isEmpty();
            }
            return inventory.preciseExtract(stack);
        }

        @Override
        public FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount, Direction side) {
            return preciseExtract(predicate, maxAmount);
        }

        @Override
        public FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.preciseExtract(predicate, maxAmount);
        }

        @Override
        public boolean preciseInsert(FluidStack stack, Direction side) {
            return preciseInsert(stack);
        }

        @Override
        public boolean preciseInsert(FluidStack stack) {
            if (!canInsert) {
                return stack.isEmpty();
            }
            return inventory.preciseInsert(stack);
        }

        @Override
        public boolean preciseInsert(FluidStack stack, int maxAmount, Direction side) {
            return preciseInsert(stack, maxAmount);
        }

        @Override
        public boolean preciseInsert(FluidStack stack, int maxAmount) {
            if (!canInsert) {
                return stack.isEmpty();
            }
            return inventory.preciseInsert(stack, maxAmount);
        }

        @Override
        public boolean preciseInsert(List<FluidStack> stacks, Direction side) {
            return preciseInsert(stacks);
        }

        @Override
        public boolean preciseInsert(List<FluidStack> stacks) {
            if (!canInsert) {
                return false;
            }
            return inventory.preciseInsert(stacks);
        }

        @Override
        public FluidStack removeStack(int slot) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.removeStack(slot);
        }

        @Override
        public FluidStack removeStack(int slot, int amount) {
            if (!canExtract) {
                return FluidStack.EMPTY;
            }
            return inventory.removeStack(slot, amount);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            inventory.setStack(slot, stack);
        }

        @Override
        public int size() {
            return inventory.size();
        }

        @Override
        public Stream<FluidStack> stream() {
            return inventory.stream();
        }

        @Override
        public Stream<FluidStack> stream(Direction side) {
            return inventory.stream();
        }
    }

    protected static class Direct extends FluidInventoryWrapper<Storage<FluidVariant>, StorageView<FluidVariant>> {
        private int size;

        public Direct(Storage<FluidVariant> storage) {
            super(storage);
        }

        @Override
        protected StorageView<FluidVariant> getSlot(int slot) {
            int current = 0;
            for (StorageView<FluidVariant> view : storage) {
                if (current == slot) {
                    return view;
                }
                current++;
            }
            return null;
        }

        @Override
        protected void init() {
            int count = 0;
            int max = 0;
            for (StorageView<FluidVariant> view : storage) {
                long capacity = view.getCapacity();
                if (max < capacity) {
                    max = (int) capacity;
                }
                count++;
            }
            size = count;
            capacity = max;
        }

        @Override
        protected void insert(StorageView<FluidVariant> view, FluidVariant variant, int amount, Transaction transaction) {
            storage.insert(variant, amount, transaction);
        }

        @Override
        public int size() {
            return size;
        }
    }

    protected static class Slotted extends FluidInventoryWrapper<SlottedStorage<FluidVariant>, SingleSlotStorage<FluidVariant>> {
        public Slotted(SlottedStorage<FluidVariant> storage) {
            super(storage);
        }

        @Override
        protected SingleSlotStorage<FluidVariant> getSlot(int slot) {
            return storage.getSlot(slot);
        }

        @Override
        protected void insert(SingleSlotStorage<FluidVariant> view, FluidVariant variant, int amount, Transaction transaction) {
            view.insert(variant, amount, transaction);
        }

        @Override
        public int insertExist(FluidStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                List<SingleSlotStorage<FluidVariant>> emptys = new ArrayList<>();
                FluidVariant variant = FluidVariant.of(stack.getFluid(), stack.getComponentChanges());
                int maxAmount = stack.getAmount();
                long remaining = maxAmount;
                for (int i = 0, size = storage.getSlotCount(); i < size; i++) {
                    SingleSlotStorage<FluidVariant> target = storage.getSlot(i);
                    if (target.getAmount() == 0 || target.isResourceBlank()) {
                        emptys.add(target);
                    } else {
                        long insert = target.insert(variant, remaining, transaction);
                        if (insert == remaining) {
                            transaction.commit();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
                for (SingleSlotStorage<FluidVariant> target : emptys) {
                    long insert = target.insert(variant, remaining, transaction);
                    if (insert == remaining) {
                        transaction.commit();
                        return maxAmount;
                    }
                    remaining -= insert;
                }
                transaction.commit();
                return maxAmount - (int) remaining;
            }
        }

        @Override
        public int size() {
            return storage.getSlotCount();
        }
    }

    static class Iterator implements java.util.Iterator<FluidStack> {
        private final java.util.Iterator<StorageView<FluidVariant>> iterator;

        public Iterator(Storage<FluidVariant> storage) {
            this.iterator = storage.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public FluidStack next() {
            StorageView<FluidVariant> view = iterator.next();
            FluidVariant variant = view.getResource();
            return new FluidStack(variant.getFluid(), view.getAmount(), variant.getComponents());
        }
    }
}
