package com.zurrtum.create;

import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBogeyStyles {
    public static final Map<ResourceLocation, BogeyStyle> BOGEY_STYLES = new HashMap<>();
    public static final Map<ResourceLocation, Map<ResourceLocation, BogeyStyle>> CYCLE_GROUPS = new HashMap<>();
    private static final Map<ResourceLocation, BogeyStyle> EMPTY_GROUP = Collections.emptyMap();

    public static final ResourceLocation STANDARD_CYCLE_GROUP = ResourceLocation.fromNamespaceAndPath(MOD_ID, "standard");

    public static final BogeyStyle STANDARD = builder("standard", STANDARD_CYCLE_GROUP).displayName(Component.translatable("create.bogey.style.standard"))
        .size(AllBogeySizes.SMALL, AllBlocks.SMALL_BOGEY).size(AllBogeySizes.LARGE, AllBlocks.LARGE_BOGEY).build();

    public static Map<ResourceLocation, BogeyStyle> getCycleGroup(ResourceLocation cycleGroup) {
        return CYCLE_GROUPS.getOrDefault(cycleGroup, EMPTY_GROUP);
    }

    private static BogeyStyle.Builder builder(String name, ResourceLocation cycleGroup) {
        return new BogeyStyle.Builder(ResourceLocation.fromNamespaceAndPath(MOD_ID, name), cycleGroup);
    }

    public static void register() {
    }
}
