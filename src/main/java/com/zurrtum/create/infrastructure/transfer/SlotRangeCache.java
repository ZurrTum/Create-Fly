package com.zurrtum.create.infrastructure.transfer;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class SlotRangeCache {
    public static final int[] EMPTY = new int[0];
    private static final Int2ObjectOpenHashMap<int[]> CACHE = new Int2ObjectOpenHashMap<>();

    public static int[] get(int size) {
        return CACHE.computeIfAbsent(
            size, key -> {
                int[] array = new int[key];
                for (int i = 0; i < key; i++) {
                    array[i] = i;
                }
                return array;
            }
        );
    }
}
