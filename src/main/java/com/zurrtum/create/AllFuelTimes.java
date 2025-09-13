package com.zurrtum.create;

import net.minecraft.item.ItemConvertible;

import java.util.IdentityHashMap;
import java.util.Map;

public class AllFuelTimes {
    public static final Map<ItemConvertible, Integer> ALL = new IdentityHashMap<>();

    public static void register() {
        ALL.put(AllItems.BLAZE_CAKE, 6400);
        ALL.put(AllItems.CREATIVE_BLAZE_CAKE, Integer.MAX_VALUE);
        ALL.put(AllItems.CARDBOARD, 1000);
        ALL.put(AllItems.CARDBOARD_BLOCK, 4000);
        ALL.put(AllItems.CARDBOARD_SWORD, 1000);
        ALL.put(AllItems.CARDBOARD_HELMET, 1000);
        ALL.put(AllItems.CARDBOARD_CHESTPLATE, 1000);
        ALL.put(AllItems.CARDBOARD_LEGGINGS, 1000);
        ALL.put(AllItems.CARDBOARD_BOOTS, 1000);
        ALL.put(AllItems.BOUND_CARDBOARD_BLOCK, 4000);
    }
}
