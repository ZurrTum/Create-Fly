package com.zurrtum.create.content.trains.bogey;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBogeySizes {
    private static final Map<ResourceLocation, BogeySize> BOGEY_SIZES = new HashMap<>();
    private static final List<BogeySize> SORTED_INCREASING = new ArrayList<>();
    private static final List<BogeySize> SORTED_DECREASING = new ArrayList<>();
    @UnmodifiableView
    private static final Map<ResourceLocation, BogeySize> BOGEY_SIZES_VIEW = Collections.unmodifiableMap(BOGEY_SIZES);
    @UnmodifiableView
    private static final List<BogeySize> SORTED_INCREASING_VIEW = Collections.unmodifiableList(SORTED_INCREASING);
    @UnmodifiableView
    private static final List<BogeySize> SORTED_DECREASING_VIEW = Collections.unmodifiableList(SORTED_DECREASING);

    public static final BogeySize SMALL = register("small", 6.5f);
    public static final BogeySize LARGE = register("large", 12.5f);

    private static BogeySize register(String id, float radius) {
        BogeySize size = new BogeySize(ResourceLocation.fromNamespaceAndPath(MOD_ID, id), radius / 16f);
        register(size);
        return size;
    }

    public static void register(BogeySize size) {
        ResourceLocation id = size.id();
        if (BOGEY_SIZES.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        BOGEY_SIZES.put(id, size);

        SORTED_INCREASING.add(size);
        SORTED_DECREASING.add(size);
        SORTED_INCREASING.sort(Comparator.comparing(BogeySize::wheelRadius));
        SORTED_DECREASING.sort(Comparator.comparing(BogeySize::wheelRadius).reversed());
    }

    @UnmodifiableView
    public static Map<ResourceLocation, BogeySize> all() {
        return BOGEY_SIZES_VIEW;
    }

    @UnmodifiableView
    public static List<BogeySize> allSortedIncreasing() {
        return SORTED_INCREASING_VIEW;
    }

    @UnmodifiableView
    public static List<BogeySize> allSortedDecreasing() {
        return SORTED_DECREASING_VIEW;
    }

    public static void register() {
    }
}
