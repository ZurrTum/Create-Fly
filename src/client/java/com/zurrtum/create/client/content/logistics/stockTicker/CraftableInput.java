package com.zurrtum.create.client.content.logistics.stockTicker;

import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public record CraftableInput(Object2ObjectMap<List<ItemStack>, IntList> data, boolean crafting) {
    private static final InputStrategy STACK_STRATEGY = new InputStrategy();

    public void add(List<ItemStack> stacks, int index) {
        data.computeIfAbsent(stacks, CraftableInput::createIntArray).add(index);
    }

    public ObjectSet<Object2ObjectMap.Entry<List<ItemStack>, IntList>> entrySet() {
        return data.object2ObjectEntrySet();
    }

    public IntSet getMissing(List<BigItemStack> inputs) {
        IntSet missing = new IntOpenHashSet();
        data.forEach((stacks, indices) -> {
            int count = stacks.getFirst().getCount();
            int size = indices.size();
            int remaining = count * size;
            for (BigItemStack stack : inputs) {
                if (contains(stacks, stack.stack)) {
                    remaining -= stack.count;
                    if (remaining <= 0) {
                        return;
                    }
                }
            }
            missing.addAll(indices.subList(size - (remaining + count - 1) / count, size));
        });
        return missing;
    }

    public PackageOrder getPattern(List<BigItemStack> inputs) {
        List<BigItemStack> mutableInputs = BigItemStack.duplicateWrappers(inputs);
        BigItemStack empty = new BigItemStack(ItemStack.EMPTY, 1);
        BigItemStack[] pattern = new BigItemStack[]{empty, empty, empty, empty, empty, empty, empty, empty, empty};
        data.forEach((stacks, indices) -> {
            IntListIterator iterator = indices.iterator();
            for (BigItemStack bigItemStack : mutableInputs) {
                int count = bigItemStack.count;
                if (count > 0 && contains(stacks, bigItemStack.stack)) {
                    BigItemStack ingredient = new BigItemStack(bigItemStack.stack, 1);
                    do {
                        count -= 1;
                        pattern[iterator.nextInt()] = ingredient;
                    } while (iterator.hasNext() && count > 0);
                    bigItemStack.count = count;
                    if (!iterator.hasNext()) {
                        break;
                    }
                }
            }
        });
        return new PackageOrder(Arrays.asList(pattern));
    }

    public static CraftableInput create(boolean crafting) {
        return new CraftableInput(new Object2ObjectOpenCustomHashMap<>(STACK_STRATEGY), crafting);
    }

    public static boolean contains(List<ItemStack> list, ItemStack stack) {
        for (ItemStack item : list) {
            if (ItemStack.isSameItemSameComponents(item, stack)) {
                return true;
            }
        }
        return false;
    }

    private static IntArrayList createIntArray(List<ItemStack> stacks) {
        return new IntArrayList();
    }

    public static class InputStrategy implements Object2IntOpenCustomHashMap.Strategy<List<ItemStack>> {
        @Override
        public int hashCode(List<ItemStack> list) {
            int size = list.size();
            int[] codes = new int[size];
            for (int i = 0; i < size; i++) {
                codes[i] = ItemStack.hashItemAndComponents(list.get(i));
            }
            Arrays.sort(codes);
            int hash = 0;
            for (int i = 0; i < size; i++) {
                hash = hash * 31 + codes[i];

            }
            return hash;
        }

        @Override
        public boolean equals(@Nullable List<ItemStack> a, @Nullable List<ItemStack> b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            int size = a.size();
            if (b.size() != size) {
                return false;
            }
            if (size == 1) {
                return ItemStack.isSameItemSameComponents(a.getFirst(), b.getFirst());
            }
            int end = size - 1;
            List<ItemStack> queue = new LinkedList<>();
            ItemStack current = b.getFirst();
            int index = 1;
            ItemStack stack;
            for (int i = 0; i < end; i++) {
                stack = a.get(i);
                if (ItemStack.isSameItemSameComponents(stack, current)) {
                    current = b.get(index++);
                    continue;
                }
                queue.add(stack);
            }
            stack = a.get(end);
            if (ItemStack.isSameItemSameComponents(stack, current)) {
                if (index == size) {
                    return true;
                }
                current = b.get(index++);
            } else {
                if (queue.size() == end) {
                    return false;
                }
                queue.add(stack);
            }
            Iterator<ItemStack> iterator;
            Find:
            while (index <= end) {
                iterator = queue.iterator();
                do {
                    stack = iterator.next();
                    if (ItemStack.isSameItemSameComponents(stack, current)) {
                        iterator.remove();
                        current = b.get(index++);
                        continue Find;
                    }
                } while (iterator.hasNext());
                return false;
            }
            return ItemStack.isSameItemSameComponents(queue.getFirst(), current);
        }
    }
}
