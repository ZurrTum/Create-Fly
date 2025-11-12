package com.zurrtum.create.infrastructure.worldgen;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class AllFeatures {
    public static final LayeredOreFeature LAYERED_ORE = register("layered_ore", new LayeredOreFeature());

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(BuiltInRegistries.FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, name), feature);
    }

    public static void register() {
    }
}
