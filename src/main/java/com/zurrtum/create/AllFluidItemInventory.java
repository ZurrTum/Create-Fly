package com.zurrtum.create;

import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventoryWrapper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.Supplier;

public class AllFluidItemInventory {
    public static Map<Item, Entry> ALL = new Reference2ObjectArrayMap<>();

    public static boolean has(ItemStack stack) {
        return ALL.containsKey(stack.getItem());
    }

    public static FluidItemInventory of(ItemStack stack) {
        Entry entry = ALL.get(stack.getItem());
        if (entry == null) {
            return null;
        }
        return entry.of(stack);
    }

    private static void register(Item type, Supplier<FluidItemInventoryWrapper> factory) {
        ALL.put(type, new Entry(factory));
    }

    public static void register() {
        register(Items.BUCKET, BucketFluidInventory::new);
        register(Items.WATER_BUCKET, BucketFluidInventory::new);
        register(Items.LAVA_BUCKET, BucketFluidInventory::new);
        register(Items.MILK_BUCKET, BucketFluidInventory::new);
        register(AllItems.HONEY_BUCKET, BucketFluidInventory::new);
        register(AllItems.CHOCOLATE_BUCKET, BucketFluidInventory::new);
    }

    public static class Entry {
        private final Supplier<FluidItemInventoryWrapper> factory;
        private final Deque<FluidItemInventoryWrapper> pool = new ArrayDeque<>();

        public Entry(Supplier<FluidItemInventoryWrapper> factory) {
            this.factory = factory;
        }

        public FluidItemInventory of(ItemStack stack) {
            FluidItemInventoryWrapper inventory = pool.pollFirst();
            if (inventory == null) {
                inventory = factory.get();
                inventory.release = pool::addLast;
            }
            inventory.stack = stack;
            return inventory;
        }
    }
}
