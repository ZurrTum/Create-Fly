package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldAccess;

public class WorldHelper {
    public static Identifier getDimensionID(WorldAccess world) {
        return world.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE).getId(world.getDimension());
    }
}
