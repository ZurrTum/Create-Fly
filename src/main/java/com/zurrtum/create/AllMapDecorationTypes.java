package com.zurrtum.create;

import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllMapDecorationTypes {
    public static final RegistryEntry<MapDecorationType> STATION_MAP_DECORATION = register("station", true, -1, true, false);

    private static RegistryEntry<MapDecorationType> register(
        String id,
        boolean showOnItemFrame,
        int mapColor,
        boolean trackCount,
        boolean explorationMapElement
    ) {
        Identifier key = Identifier.of(MOD_ID, id);
        RegistryKey<MapDecorationType> registryKey = RegistryKey.of(RegistryKeys.MAP_DECORATION_TYPE, key);
        MapDecorationType mapDecorationType = new MapDecorationType(key, showOnItemFrame, mapColor, explorationMapElement, trackCount);
        return Registry.registerReference(Registries.MAP_DECORATION_TYPE, registryKey, mapDecorationType);
    }

    public static void register() {
    }
}
