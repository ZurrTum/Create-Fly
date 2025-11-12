package com.zurrtum.create;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.world.level.ItemLike;

public class AllFuelTimes {
    public static final Map<ItemLike, Integer> ALL = new IdentityHashMap<>();

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
