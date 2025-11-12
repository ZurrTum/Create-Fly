package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public class WorldHelper {
    public static ResourceLocation getDimensionID(LevelAccessor world) {
        return world.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).getKey(world.dimensionType());
    }
}
