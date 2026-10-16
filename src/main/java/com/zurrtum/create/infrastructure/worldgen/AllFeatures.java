package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFeatures {
    public static final Identifier LAYERED_ORE = register("layered_ore", LayeredOreFeature.CODEC);
    public static final ResourceKey<Feature> ZINC_ORE = register("zinc_ore");
    public static final ResourceKey<Feature> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final ResourceKey<Feature> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static ResourceKey<Feature> register(String id) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static Identifier register(String name, MapCodec<? extends Feature> codec) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        Registry.register(BuiltInRegistries.FEATURE_TYPE, id, codec);
        return id;
    }

    public static void register() {
    }
}
