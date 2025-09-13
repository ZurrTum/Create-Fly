package com.zurrtum.create.foundation.item;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;

import java.util.*;
import java.util.stream.Stream;

public class CountedItemStackList {

    Map<Item, Set<ItemStackEntry>> items = new HashMap<>();

    public CountedItemStackList(Inventory inventory, ServerFilteringBehaviour filteringBehaviour) {
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            ItemStack extractItem = inventory.getStack(slot);
            if (filteringBehaviour.test(extractItem))
                add(extractItem);
        }
    }

    public Stream<IntAttached<MutableText>> getTopNames(int limit) {
        return items.values().stream().flatMap(Collection::stream).sorted(IntAttached.comparator()).limit(limit)
            .map(entry -> IntAttached.with(entry.count(), entry.stack().getName().copy()));
    }

    public void add(ItemStack stack) {
        add(stack, stack.getCount());
    }

    public void add(ItemStack stack, int amount) {
        if (stack.isEmpty())
            return;

        Set<ItemStackEntry> stackSet = getOrCreateItemSet(stack);
        for (ItemStackEntry entry : stackSet) {
            if (!entry.matches(stack))
                continue;
            entry.grow(amount);
            return;
        }
        stackSet.add(new ItemStackEntry(stack, amount));
    }

    private Set<ItemStackEntry> getOrCreateItemSet(ItemStack stack) {
        if (!items.containsKey(stack.getItem()))
            items.put(stack.getItem(), new HashSet<>());
        return getItemSet(stack);
    }

    private Set<ItemStackEntry> getItemSet(ItemStack stack) {
        return items.get(stack.getItem());
    }

    public static class ItemStackEntry extends IntAttached<ItemStack> {

        public ItemStackEntry(ItemStack stack) {
            this(stack, stack.getCount());
        }

        public ItemStackEntry(ItemStack stack, int amount) {
            super(amount, stack);
        }

        public boolean matches(ItemStack other) {
            return ItemStack.areItemsAndComponentsEqual(other, stack());
        }

        public ItemStack stack() {
            return getSecond();
        }

        public void grow(int amount) {
            setFirst(getFirst() + amount);
        }

        public int count() {
            return getFirst();
        }

    }

}
