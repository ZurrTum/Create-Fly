package com.zurrtum.create;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class AllMapDecorationTypes {
    public static final Holder<MapDecorationType> STATION_MAP_DECORATION = register("station", true, -1, true, false);

    private static Holder<MapDecorationType> register(
        String id,
        boolean showOnItemFrame,
        int mapColor,
        boolean trackCount,
        boolean explorationMapElement
    ) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
        ResourceKey<MapDecorationType> registryKey = ResourceKey.create(Registries.MAP_DECORATION_TYPE, key);
        MapDecorationType mapDecorationType = new MapDecorationType(key, showOnItemFrame, mapColor, explorationMapElement, trackCount);
        return Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, registryKey, mapDecorationType);
    }

    public static void register() {
    }
}
