package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacementModifiers {
    public static final PlacementModifierType<ConfigPlacementFilter> CONFIG_FILTER = register("config_filter", ConfigPlacementFilter.CODEC);

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, Identifier.of(MOD_ID, id), () -> codec);
    }

    public static void register() {
    }
}
