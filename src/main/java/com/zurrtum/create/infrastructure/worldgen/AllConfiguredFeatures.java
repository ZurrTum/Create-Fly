package com.zurrtum.create.infrastructure.worldgen;

import static com.zurrtum.create.Create.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class AllConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ZINC_ORE = register("zinc_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final ResourceKey<ConfiguredFeature<?, ?>> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static ResourceKey<ConfiguredFeature<?, ?>> register(String id) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static void register() {
    }
}
