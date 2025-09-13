package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> ZINC_ORE = register("zinc_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final RegistryKey<ConfiguredFeature<?, ?>> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static RegistryKey<ConfiguredFeature<?, ?>> register(String id) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(MOD_ID, id));
    }

    public static void register() {
    }
}
