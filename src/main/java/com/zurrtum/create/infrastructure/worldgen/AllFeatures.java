package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFeatures {
    public static final LayeredOreFeature LAYERED_ORE = register("layered_ore", new LayeredOreFeature());

    private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, name), feature);
    }

    public static void register() {
    }
}
