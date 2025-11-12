package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacementModifiers {
    public static final PlacementModifierType<ConfigPlacementFilter> CONFIG_FILTER = register("config_filter", ConfigPlacementFilter.CODEC);

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, id), () -> codec);
    }

    public static void register() {
    }
}
