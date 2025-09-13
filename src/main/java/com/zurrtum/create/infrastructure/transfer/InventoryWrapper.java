package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.items.ItemInventory;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class InventoryWrapper<T extends Storage<ItemVariant>, S extends StorageView<ItemVariant>> implements ItemInventory {
    protected T storage;
    protected int capacity;

    public InventoryWrapper(T storage) {
        this.storage = storage;
        init();
    }

    public static Inventory of(Storage<ItemVariant> storage) {
        InventoryWrapper<?, ?> inventory;
        if (storage instanceof SlottedStorage<ItemVariant> slottedStorage) {
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
    public void clear() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                view.extract(view.getResource(), view.getAmount(), transaction);
            }
            transaction.commit();
        }
    }

    @Override
    public int count(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount);
    }

    @Override
    public int count(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extract = storage.extract(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.abort();
            return (int) extract;
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack count(Predicate<ItemStack> predicate) {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    long extract = view.extract(variant, view.getAmount(), transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.abort();
                    return directCopy(stack, (int) extract);
                }
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.abort();
                    return directCopy(stack, (int) extract);
                }
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public int countAll(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        long count = 0;
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                if (predicate.test(((ItemVariantImpl) variant).getCachedStack())) {
                    count += view.extract(variant, view.getAmount(), transaction);
                    if (count >= maxAmount) {
                        transaction.abort();
                        return maxAmount;
                    }
                }
            }
            transaction.abort();
        }
        return (int) count;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack countAny() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                long extract = view.extract(variant, view.getAmount(), transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.abort();
                return directCopy(stack, (int) extract);
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack countAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                long extract = storage.extract(variant, maxAmount, transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.abort();
                return directCopy(stack, (int) extract);
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int countSpace(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount);
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.abort();
            return (int) insert;
        }
    }

    @Override
    public boolean countSpace(List<ItemStack> stacks) {
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
        try (Transaction transaction = Transaction.openOuter()) {
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), count, transaction);
                if (insert < count) {
                    transaction.abort();
                    return false;
                }
            } while (iterator.hasNext());
            transaction.abort();
            return true;
        }
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        return countSpace(stack, maxAmount);
    }

    @Override
    public boolean countSpace(List<ItemStack> stacks, int start, int end) {
        return countSpace(stacks);
    }

    @Override
    public int extract(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount);
    }

    @Override
    public int extract(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extract = storage.extract(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.commit();
            return (int) extract;
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack extract(Predicate<ItemStack> predicate) {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    long extract = view.extract(variant, view.getAmount(), transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.commit();
                    return directCopy(stack, (int) extract);
                }
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    transaction.commit();
                    return directCopy(stack, (int) extract);
                }
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> extract(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int extract = extract(stacks.getFirst(), count);
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
        try (Transaction transaction = Transaction.openOuter()) {
            boolean dirty = false;
            Map<ItemStack, ItemVariant> cache = new IdentityHashMap<>();
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                do {
                    Object2IntMap.Entry<ItemStack> entry = iterator.next();
                    ItemStack stack = entry.getKey();
                    int count = entry.getIntValue();
                    ItemVariant variant = cache.computeIfAbsent(stack, ItemVariant::of);
                    long extract = view.extract(variant, count, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    if (extract == count) {
                        iterator.remove();
                        if (entries.isEmpty()) {
                            transaction.commit();
                            return List.of();
                        }
                    } else {
                        entry.setValue(count - (int) extract);
                    }
                    dirty = true;
                    break;
                } while (iterator.hasNext());
            }
            if (dirty) {
                List<ItemStack> result = new ArrayList<>();
                for (Object2IntMap.Entry<ItemStack> entry : entries) {
                    ItemStack stack = entry.getKey();
                    int count = entry.getIntValue();
                    if (stack.getCount() == count) {
                        result.add(stack);
                    } else {
                        result.add(directCopy(stack, count));
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
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack extractAny() {
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                long extract = view.extract(variant, view.getAmount(), transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.commit();
                return directCopy(((ItemVariantImpl) variant).getCachedStack(), (int) extract);
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack extractAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                long extract = storage.extract(variant, maxAmount, transaction);
                if (extract == 0) {
                    continue;
                }
                transaction.commit();
                return directCopy(((ItemVariantImpl) variant).getCachedStack(), (int) extract);
            }
            transaction.abort();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        long remaining = maxAmount;
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                if (predicate.test(((ItemVariantImpl) variant).getCachedStack())) {
                    long extract = view.extract(variant, remaining, transaction);
                    if (extract == remaining) {
                        transaction.commit();
                        return maxAmount;
                    }
                    remaining -= extract;
                }
            }
            transaction.commit();
        }
        return (int) (maxAmount - remaining);
    }

    @Override
    public int forceInsert(ItemStack stack) {
        return insert(stack);
    }

    @Override
    public int forceInsert(ItemStack stack, int maxAmount) {
        return insert(stack, maxAmount);
    }

    @Override
    public boolean forcePreciseInsert(ItemStack stack) {
        return preciseInsert(stack);
    }

    @Override
    public boolean forcePreciseInsert(ItemStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount);
    }

    @Override
    public int getMaxCountPerStack() {
        return capacity;
    }

    protected abstract S getSlot(int slot);

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= size()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = getSlot(slot);
        if (view == null) {
            return ItemStack.EMPTY;
        }
        ItemVariant variant = view.getResource();
        return new ItemStack(variant.getRegistryEntry(), (int) view.getAmount(), variant.getComponents());
    }

    protected void init() {
        int max = 0;
        for (StorageView<ItemVariant> view : storage) {
            long capacity = view.getCapacity();
            if (max > capacity) {
                max = (int) capacity;
            }
        }
        capacity = max;
    }

    protected abstract void insert(S view, ItemVariant variant, int amount, Transaction transaction);

    @Override
    public int insert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount);
    }

    @Override
    public int insert(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.commit();
            return (int) insert;
        }
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stacks.getFirst(), count);
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
        try (Transaction transaction = Transaction.openOuter()) {
            boolean dirty = false;
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), count, transaction);
                if (insert == count) {
                    iterator.remove();
                    if (entries.isEmpty()) {
                        transaction.commit();
                        return List.of();
                    }
                    dirty = true;
                } else if (insert != 0) {
                    entry.setValue(count - (int) insert);
                    dirty = true;
                }
            } while (iterator.hasNext());
            if (dirty) {
                List<ItemStack> result = new ArrayList<>();
                for (Object2IntMap.Entry<ItemStack> entry : entries) {
                    ItemStack stack = entry.getKey();
                    int count = entry.getIntValue();
                    if (stack.getCount() == count) {
                        result.add(stack);
                    } else {
                        result.add(directCopy(stack, count));
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
    public int insert(ItemStack stack, int maxAmount, int start, int end) {
        return insert(stack, maxAmount);
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        return insert(stacks);
    }

    @Override
    public int insertExist(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insertExist(stack, maxAmount);
    }

    @Override
    public int insertExist(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
            transaction.commit();
            return (int) insert;
        }
    }

    @Override
    public boolean isEmpty() {
        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            return false;
        }
        return true;
    }

    @Override
    @NotNull
    public java.util.Iterator<ItemStack> iterator() {
        return storage.supportsExtraction() ? new Iterator(storage) : Collections.emptyIterator();
    }

    @Override
    public boolean preciseExtract(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            int amount = stack.getCount();
            long extract = storage.extract(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), amount, transaction);
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
    @SuppressWarnings("UnstableApiUsage")
    public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    long extract = storage.extract(variant, maxAmount, transaction);
                    if (extract == 0) {
                        continue;
                    }
                    if (extract == maxAmount) {
                        transaction.commit();
                        return directCopy(stack, maxAmount);
                    } else {
                        transaction.abort();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean preciseInsert(ItemStack stack) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount);
    }

    @Override
    public boolean preciseInsert(ItemStack stack, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), maxAmount, transaction);
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
    public boolean preciseInsert(List<ItemStack> stacks) {
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
        try (Transaction transaction = Transaction.openOuter()) {
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                long insert = storage.insert(ItemVariant.of(stack.getItem(), stack.getComponentChanges()), count, transaction);
                if (insert < count) {
                    transaction.abort();
                    return false;
                }
            } while (iterator.hasNext());
            transaction.commit();
            return true;
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot >= size()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = getSlot(slot);
        if (view == null) {
            return ItemStack.EMPTY;
        }
        ItemVariant variant = view.getResource();
        long amount = view.getAmount();
        if (variant.isBlank() || amount == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            amount = view.extract(variant, amount, transaction);
            transaction.commit();
        }
        return new ItemStack(variant.getRegistryEntry(), (int) amount, variant.getComponents());
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot >= size()) {
            return ItemStack.EMPTY;
        }
        StorageView<ItemVariant> view = getSlot(slot);
        if (view == null) {
            return ItemStack.EMPTY;
        }
        ItemVariant variant = view.getResource();
        if (variant.isBlank() || view.getAmount() == 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            amount = (int) view.extract(variant, amount, transaction);
            transaction.commit();
        }
        return new ItemStack(variant.getRegistryEntry(), amount, variant.getComponents());
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= size()) {
            return;
        }
        S view = getSlot(slot);
        if (view == null) {
            return;
        }
        ItemVariant variant = view.getResource();
        try (Transaction transaction = Transaction.openOuter()) {
            if (variant.isBlank() || view.getAmount() == 0) {
                if (stack.isEmpty()) {
                    return;
                }
                insert(view, ItemVariant.of(stack.getItem(), stack.getComponentChanges()), stack.getCount(), transaction);
            } else if (variant.matches(stack)) {
                int amount = stack.getCount();
                int targetCount = (int) view.getAmount();
                if (amount == targetCount) {
                    return;
                }
                int change = targetCount - amount;
                if (amount < targetCount) {
                    insert(view, variant, change, transaction);
                } else {
                    view.extract(variant, change, transaction);
                }
            } else {
                view.extract(variant, view.getAmount(), transaction);
                insert(view, ItemVariant.of(stack.getItem(), stack.getComponentChanges()), stack.getCount(), transaction);
            }
            transaction.commit();
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            ItemVariant variant = view.getResource();
            ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
            if (predicate.test(stack)) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long amount = view.getAmount();
                    ItemStack replace = update.apply(new ItemStack(variant.getRegistryEntry(), (int) amount, variant.getComponents()));
                    if (ItemStack.areItemsAndComponentsEqual(stack, replace)) {
                        int count = replace.getCount();
                        if (count == amount) {
                            return true;
                        }
                        long change;
                        if (count < amount) {
                            change = view.extract(variant, amount - count, transaction);
                        } else {
                            change = storage.insert(variant, count - amount, transaction);
                        }
                        if (change != 0) {
                            transaction.commit();
                            return true;
                        }
                    } else {
                        long extract = view.extract(variant, amount, transaction);
                        if (extract != 0) {
                            if (replace.isEmpty()) {
                                transaction.commit();
                                return true;
                            }
                            long insert = storage.insert(
                                ItemVariant.of(replace.getItem(), replace.getComponentChanges()),
                                replace.getCount(),
                                transaction
                            );
                            if (insert != 0) {
                                transaction.commit();
                                return true;
                            }
                        }
                        transaction.abort();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Stream<ItemStack> stream() {
        return storage.supportsExtraction() ? ItemInventory.super.stream() : Stream.empty();
    }

    public static class Filter implements SidedInventory {
        private final int[] slots;
        private final boolean canInsert;
        private final boolean canExtract;
        private final InventoryWrapper<?, ?> inventory;

        public Filter(InventoryWrapper<?, ?> inventory) {
            this.inventory = inventory;
            this.slots = SlotRangeCache.get(inventory.size());
            this.canInsert = inventory.storage.supportsInsertion();
            this.canExtract = inventory.storage.supportsExtraction();
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return canExtract;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return canInsert;
        }

        @Override
        public void clear() {
            if (!canExtract) {
                return;
            }
            inventory.clear();
        }

        @Override
        public int count(ItemStack stack, Direction side) {
            return count(stack);
        }

        @Override
        public int count(ItemStack stack) {
            if (!canExtract) {
                return 0;
            }
            return inventory.count(stack);
        }

        @Override
        public int count(ItemStack stack, int maxAmount, Direction side) {
            return count(stack, maxAmount);
        }

        @Override
        public int count(ItemStack stack, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.count(stack, maxAmount);
        }

        @Override
        public ItemStack count(Predicate<ItemStack> predicate, Direction side) {
            return count(predicate);
        }

        @Override
        public ItemStack count(Predicate<ItemStack> predicate) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.count(predicate);
        }

        @Override
        public ItemStack count(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
            return count(predicate, maxAmount);
        }

        @Override
        public ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.count(predicate, maxAmount);
        }

        @Override
        public int countAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
            return countAll(predicate, maxAmount);
        }

        @Override
        public int countAll(Predicate<ItemStack> predicate, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.countAll(predicate, maxAmount);
        }

        @Override
        public ItemStack countAny(Direction side) {
            return countAny();
        }

        @Override
        public ItemStack countAny() {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.countAny();
        }

        @Override
        public ItemStack countAny(int maxAmount, Direction side) {
            return countAny(maxAmount);
        }

        @Override
        public ItemStack countAny(int maxAmount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.countAny(maxAmount);
        }

        @Override
        public int countSpace(ItemStack stack, Direction side) {
            return countSpace(stack);
        }

        @Override
        public int countSpace(ItemStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.countSpace(stack);
        }

        @Override
        public int countSpace(ItemStack stack, int maxAmount, Direction side) {
            return countSpace(stack, maxAmount);
        }

        @Override
        public int countSpace(ItemStack stack, int maxAmount) {
            if (!canInsert) {
                return 0;
            }
            return inventory.countSpace(stack, maxAmount);
        }

        @Override
        public boolean countSpace(List<ItemStack> stacks, Direction side) {
            return countSpace(stacks);
        }

        @Override
        public boolean countSpace(List<ItemStack> stacks) {
            if (!canInsert) {
                return false;
            }
            return inventory.countSpace(stacks);
        }

        @Override
        public int extract(ItemStack stack, Direction side) {
            return extract(stack);
        }

        @Override
        public int extract(ItemStack stack) {
            if (!canExtract) {
                return 0;
            }
            return inventory.extract(stack);
        }

        @Override
        public int extract(ItemStack stack, int maxAmount, Direction side) {
            return extract(stack, maxAmount);
        }

        @Override
        public int extract(ItemStack stack, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.extract(stack, maxAmount);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate, Direction side) {
            return extract(predicate);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.extract(predicate);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
            return extract(predicate, maxAmount);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.extract(predicate, maxAmount);
        }

        @Override
        public List<ItemStack> extract(List<ItemStack> stacks, Direction side) {
            return extract(stacks);
        }

        @Override
        public List<ItemStack> extract(List<ItemStack> stacks) {
            if (!canExtract) {
                return stacks;
            }
            return inventory.extract(stacks);
        }

        @Override
        public int extractAll(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
            return extractAll(predicate, maxAmount);
        }

        @Override
        public int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
            if (!canExtract) {
                return 0;
            }
            return inventory.extractAll(predicate, maxAmount);
        }

        @Override
        public ItemStack extractAny(Direction side) {
            return extractAny();
        }

        @Override
        public ItemStack extractAny() {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.extractAny();
        }

        @Override
        public ItemStack extractAny(int maxAmount, Direction side) {
            return extractAny(maxAmount);
        }

        @Override
        public ItemStack extractAny(int maxAmount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.extractAny(maxAmount);
        }

        @Override
        public int forceInsert(ItemStack stack) {
            return inventory.forceInsert(stack);
        }

        @Override
        public int forceInsert(ItemStack stack, int maxAmount) {
            return inventory.forceInsert(stack, maxAmount);
        }

        @Override
        public boolean forcePreciseInsert(ItemStack stack) {
            return inventory.forcePreciseInsert(stack);
        }

        @Override
        public boolean forcePreciseInsert(ItemStack stack, int maxAmount) {
            return inventory.forcePreciseInsert(stack, maxAmount);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return slots;
        }

        @Override
        public int getMaxCountPerStack() {
            return inventory.getMaxCountPerStack();
        }

        @Override
        public ItemStack getStack(int slot) {
            return inventory.getStack(slot);
        }

        @Override
        public int insert(ItemStack stack, Direction side) {
            return insert(stack);
        }

        @Override
        public int insert(ItemStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insert(stack);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, Direction side) {
            return insert(stack, maxAmount);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insert(stack, maxAmount);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, int start, int end, Direction side) {
            return insert(stack, maxAmount);
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, int start, int end) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insert(stack, maxAmount, start, end);
        }

        @Override
        public List<ItemStack> insert(List<ItemStack> stacks, Direction side) {
            return insert(stacks);
        }

        @Override
        public List<ItemStack> insert(List<ItemStack> stacks) {
            if (!canInsert) {
                return stacks;
            }
            return inventory.insert(stacks);
        }

        @Override
        public List<ItemStack> insert(List<ItemStack> stacks, int start, int end, Direction side) {
            return insert(stacks, start, end);
        }

        @Override
        public List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
            if (!canInsert) {
                return stacks;
            }
            return inventory.insert(stacks, start, end);
        }

        @Override
        public int insertExist(ItemStack stack, Direction side) {
            return insertExist(stack);
        }

        @Override
        public int insertExist(ItemStack stack) {
            if (!canInsert) {
                return 0;
            }
            return inventory.insertExist(stack);
        }

        @Override
        public int insertExist(ItemStack stack, int maxAmount, Direction side) {
            if (!canInsert) {
                return 0;
            }
            return insertExist(stack, maxAmount);
        }

        @Override
        public int insertExist(ItemStack stack, int maxAmount) {
            return inventory.insertExist(stack, maxAmount);
        }

        @Override
        public boolean isEmpty() {
            return inventory.isEmpty();
        }

        @Override
        public @NotNull java.util.Iterator<ItemStack> iterator() {
            return inventory.iterator();
        }

        @Override
        public @NotNull java.util.Iterator<ItemStack> iterator(Direction side) {
            return inventory.iterator();
        }

        @Override
        public boolean preciseExtract(ItemStack stack, Direction side) {
            return preciseExtract(stack);
        }

        @Override
        public boolean preciseExtract(ItemStack stack) {
            if (!canExtract) {
                return stack.isEmpty();
            }
            return inventory.preciseExtract(stack);
        }

        @Override
        public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, Direction side) {
            return preciseExtract(predicate, maxAmount);
        }

        @Override
        public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.preciseExtract(predicate, maxAmount);
        }

        @Override
        public boolean preciseInsert(ItemStack stack, Direction side) {
            return preciseInsert(stack);
        }

        @Override
        public boolean preciseInsert(ItemStack stack) {
            if (!canInsert) {
                return stack.isEmpty();
            }
            return inventory.preciseInsert(stack);
        }

        @Override
        public boolean preciseInsert(ItemStack stack, int maxAmount, Direction side) {
            return preciseInsert(stack, maxAmount);
        }

        @Override
        public boolean preciseInsert(ItemStack stack, int maxAmount) {
            if (!canInsert) {
                return stack.isEmpty();
            }
            return inventory.preciseInsert(stack, maxAmount);
        }

        @Override
        public boolean preciseInsert(List<ItemStack> stacks, Direction side) {
            return preciseInsert(stacks);
        }

        @Override
        public boolean preciseInsert(List<ItemStack> stacks) {
            if (!canInsert) {
                return false;
            }
            return inventory.preciseInsert(stacks);
        }

        @Override
        public ItemStack removeStack(int slot) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.removeStack(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            if (!canExtract) {
                return ItemStack.EMPTY;
            }
            return inventory.removeStack(slot, amount);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            inventory.setStack(slot, stack);
        }

        @Override
        public int size() {
            return inventory.size();
        }

        @Override
        public Stream<ItemStack> stream() {
            return inventory.stream();
        }

        @Override
        public Stream<ItemStack> stream(Direction side) {
            return inventory.stream();
        }
    }

    protected static class Direct extends InventoryWrapper<Storage<ItemVariant>, StorageView<ItemVariant>> {
        private int size;

        public Direct(Storage<ItemVariant> storage) {
            super(storage);
        }

        @Override
        protected StorageView<ItemVariant> getSlot(int slot) {
            int current = 0;
            for (StorageView<ItemVariant> view : storage) {
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
            for (StorageView<ItemVariant> view : storage) {
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
        protected void insert(StorageView<ItemVariant> view, ItemVariant variant, int amount, Transaction transaction) {
            storage.insert(variant, amount, transaction);
        }

        @Override
        public int size() {
            return size;
        }
    }

    protected static class Slotted extends InventoryWrapper<SlottedStorage<ItemVariant>, SingleSlotStorage<ItemVariant>> {
        public Slotted(SlottedStorage<ItemVariant> storage) {
            super(storage);
        }

        @Override
        protected SingleSlotStorage<ItemVariant> getSlot(int slot) {
            return storage.getSlot(slot);
        }

        @Override
        protected void insert(SingleSlotStorage<ItemVariant> view, ItemVariant variant, int amount, Transaction transaction) {
            view.insert(variant, amount, transaction);
        }

        @Override
        public int insertExist(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                List<SingleSlotStorage<ItemVariant>> emptys = new ArrayList<>();
                ItemVariant variant = ItemVariant.of(stack.getItem(), stack.getComponentChanges());
                int maxAmount = stack.getCount();
                long remaining = maxAmount;
                for (int i = 0, size = storage.getSlotCount(); i < size; i++) {
                    SingleSlotStorage<ItemVariant> target = storage.getSlot(i);
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
                for (SingleSlotStorage<ItemVariant> target : emptys) {
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
        public int countSpace(ItemStack stack, int maxAmount, int start, int end) {
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(stack.getItem(), stack.getComponentChanges());
                long remaining = maxAmount;
                for (int i = start; i <= end; i++) {
                    SingleSlotStorage<ItemVariant> target = storage.getSlot(i);
                    long insert = target.insert(variant, remaining, transaction);
                    if (insert == remaining) {
                        transaction.abort();
                        return maxAmount;
                    }
                    remaining -= insert;
                }
                transaction.abort();
                return maxAmount - (int) remaining;
            }
        }

        @Override
        public boolean countSpace(List<ItemStack> stacks, int start, int end) {
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
            try (Transaction transaction = Transaction.openOuter()) {
                ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                do {
                    Object2IntMap.Entry<ItemStack> entry = iterator.next();
                    ItemStack stack = entry.getKey();
                    int count = entry.getIntValue();
                    ItemVariant variant = ItemVariant.of(stack.getItem(), stack.getComponentChanges());
                    long remaining = count;
                    for (int i = start; i <= end; i++) {
                        SingleSlotStorage<ItemVariant> target = storage.getSlot(i);
                        remaining -= target.insert(variant, remaining, transaction);
                        if (remaining == 0) {
                            break;
                        }
                    }
                    if (remaining == 0) {
                        iterator.remove();
                        if (entries.isEmpty()) {
                            transaction.abort();
                            return true;
                        }
                    } else if (remaining != count) {
                        entry.setValue((int) remaining);
                    }
                } while (iterator.hasNext());
                transaction.abort();
                return false;
            }
        }

        @Override
        public int insert(ItemStack stack, int maxAmount, int start, int end) {
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(stack.getItem(), stack.getComponentChanges());
                long remaining = maxAmount;
                for (int i = start; i <= end; i++) {
                    SingleSlotStorage<ItemVariant> target = storage.getSlot(i);
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
        public List<ItemStack> insert(List<ItemStack> stacks) {
            int listSize = stacks.size();
            if (listSize == 0) {
                return stacks;
            }
            if (listSize == 1) {
                ItemStack stack = stacks.getFirst();
                int count = stack.getCount();
                int insert = insert(stacks.getFirst(), count);
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
            try (Transaction transaction = Transaction.openOuter()) {
                boolean dirty = false;
                Map<ItemStack, ItemVariant> cache = new IdentityHashMap<>();
                for (int i = 0, size = storage.getSlotCount(); i < size; i++) {
                    SingleSlotStorage<ItemVariant> slot = storage.getSlot(i);
                    ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                    do {
                        Object2IntMap.Entry<ItemStack> entry = iterator.next();
                        ItemStack stack = entry.getKey();
                        int count = entry.getIntValue();
                        ItemVariant variant = cache.computeIfAbsent(stack, ItemVariant::of);
                        long insert = slot.insert(variant, count, transaction);
                        if (insert == 0) {
                            continue;
                        }
                        if (insert == count) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                transaction.commit();
                                return List.of();
                            }
                        } else {
                            entry.setValue(count - (int) insert);
                        }
                        dirty = true;
                        break;
                    } while (iterator.hasNext());
                }
                if (dirty) {
                    List<ItemStack> result = new ArrayList<>();
                    for (Object2IntMap.Entry<ItemStack> entry : entries) {
                        ItemStack stack = entry.getKey();
                        int count = entry.getIntValue();
                        if (stack.getCount() == count) {
                            result.add(stack);
                        } else {
                            result.add(directCopy(stack, count));
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
        public List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
            int listSize = stacks.size();
            if (listSize == 0) {
                return stacks;
            }
            if (listSize == 1) {
                ItemStack stack = stacks.getFirst();
                int count = stack.getCount();
                int insert = insert(stacks.getFirst(), count, start, end);
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
            try (Transaction transaction = Transaction.openOuter()) {
                boolean dirty = false;
                ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                do {
                    Object2IntMap.Entry<ItemStack> entry = iterator.next();
                    ItemStack stack = entry.getKey();
                    int count = entry.getIntValue();
                    ItemVariant variant = ItemVariant.of(stack.getItem(), stack.getComponentChanges());
                    long remaining = count;
                    for (int i = start; i <= end; i++) {
                        SingleSlotStorage<ItemVariant> target = storage.getSlot(i);
                        remaining -= target.insert(variant, remaining, transaction);
                        if (remaining == 0) {
                            break;
                        }
                    }
                    if (remaining == 0) {
                        iterator.remove();
                        if (entries.isEmpty()) {
                            transaction.commit();
                            return List.of();
                        }
                        dirty = true;
                    } else if (remaining != count) {
                        entry.setValue((int) remaining);
                        dirty = true;
                    }
                } while (iterator.hasNext());
                if (dirty) {
                    List<ItemStack> result = new ArrayList<>();
                    for (Object2IntMap.Entry<ItemStack> entry : entries) {
                        ItemStack stack = entry.getKey();
                        int count = entry.getIntValue();
                        if (stack.getCount() == count) {
                            result.add(stack);
                        } else {
                            result.add(directCopy(stack, count));
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
        @SuppressWarnings("UnstableApiUsage")
        public boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
            for (int i = 0, size = storage.getSlotCount(); i < size; i++) {
                SingleSlotStorage<ItemVariant> slot = storage.getSlot(i);
                ItemVariant variant = slot.getResource();
                ItemStack stack = ((ItemVariantImpl) variant).getCachedStack();
                if (predicate.test(stack)) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long amount = slot.getAmount();
                        ItemStack replace = update.apply(new ItemStack(variant.getRegistryEntry(), (int) amount, variant.getComponents()));
                        if (ItemStack.areItemsAndComponentsEqual(stack, replace)) {
                            int count = replace.getCount();
                            if (count == amount) {
                                return true;
                            }
                            long change;
                            if (count < amount) {
                                change = slot.extract(variant, amount - count, transaction);
                            } else {
                                change = slot.insert(variant, count - amount, transaction);
                            }
                            if (change != 0) {
                                transaction.commit();
                                return true;
                            }
                        } else {
                            long extract = slot.extract(variant, amount, transaction);
                            if (extract != 0) {
                                if (replace.isEmpty()) {
                                    transaction.commit();
                                    return true;
                                }
                                long insert = slot.insert(
                                    ItemVariant.of(replace.getItem(), replace.getComponentChanges()),
                                    replace.getCount(),
                                    transaction
                                );
                                if (insert != 0) {
                                    transaction.commit();
                                    return true;
                                }
                            }
                            transaction.abort();
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public int size() {
            return storage.getSlotCount();
        }
    }

    static class Iterator implements java.util.Iterator<ItemStack> {
        private final java.util.Iterator<StorageView<ItemVariant>> iterator;

        public Iterator(Storage<ItemVariant> storage) {
            this.iterator = storage.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public ItemStack next() {
            StorageView<ItemVariant> view = iterator.next();
            if (view.getAmount() == 0 || view.isResourceBlank()) {
                return ItemStack.EMPTY;
            }
            ItemVariant variant = view.getResource();
            return new ItemStack(variant.getRegistryEntry(), (int) view.getAmount(), variant.getComponents());
        }
    }
}
