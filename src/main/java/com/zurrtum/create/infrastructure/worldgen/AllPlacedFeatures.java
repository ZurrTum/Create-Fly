package com.zurrtum.create.infrastructure.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ZINC_ORE = register("zinc_ore");
    public static final ResourceKey<PlacedFeature> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final ResourceKey<PlacedFeature> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static ResourceKey<PlacedFeature> register(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static void register(RegistryAccess registryManager) {
        Registry<PlacedFeature> placed = registryManager.lookupOrThrow(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> zincOverworld = placed.get(AllPlacedFeatures.ZINC_ORE).orElseThrow();
        Holder<PlacedFeature> striatedOverworld = placed.get(AllPlacedFeatures.STRIATED_ORES_OVERWORLD).orElseThrow();
        Holder<PlacedFeature> striatedNether = placed.get(AllPlacedFeatures.STRIATED_ORES_NETHER).orElseThrow();
        int index = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
        addFeature(registryManager, LevelStem.OVERWORLD, index, List.of(zincOverworld, striatedOverworld));
        addFeature(registryManager, LevelStem.NETHER, index, List.of(striatedNether));
    }

    private static void addFeature(RegistryAccess registryManager, ResourceKey<LevelStem> options, int index, List<Holder<PlacedFeature>> entries) {
        registryManager.lookupOrThrow(Registries.LEVEL_STEM).getValue(options).generator().getBiomeSource().possibleBiomes().stream()
            .map(Holder::value).map(Biome::getGenerationSettings).forEach(settings -> {
                if (!(settings.features instanceof ArrayList<HolderSet<PlacedFeature>>)) {
                    settings.features = new ArrayList<>(settings.features);
                }
                List<HolderSet<PlacedFeature>> features = settings.features;
                int size = features.size();
                if (size <= index) {
                    for (int i = size; i < index; i++) {
                        features.add(HolderSet.direct(Collections.emptyList()));
                    }
                    features.add(HolderSet.direct(entries));
                } else {
                    HolderSet<PlacedFeature> values = features.get(index);
                    if (values != null) {
                        List<Holder<PlacedFeature>> list = new ArrayList<>(values.stream().toList());
                        list.addAll(entries);
                        values = HolderSet.direct(list);
                    } else {
                        values = HolderSet.direct(entries);
                    }
                    features.set(index, values);
                }
            });
    }

    public static void register() {
    }
}
