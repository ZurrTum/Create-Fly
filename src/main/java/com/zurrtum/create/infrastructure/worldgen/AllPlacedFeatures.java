package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacedFeatures {
    public static final RegistryKey<PlacedFeature> ZINC_ORE = register("zinc_ore");
    public static final RegistryKey<PlacedFeature> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final RegistryKey<PlacedFeature> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static RegistryKey<PlacedFeature> register(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, id));
    }

    public static void register(DynamicRegistryManager registryManager) {
        Registry<PlacedFeature> placed = registryManager.getOrThrow(RegistryKeys.PLACED_FEATURE);
        RegistryEntry<PlacedFeature> zincOverworld = placed.getOptional(AllPlacedFeatures.ZINC_ORE).orElseThrow();
        RegistryEntry<PlacedFeature> striatedOverworld = placed.getOptional(AllPlacedFeatures.STRIATED_ORES_OVERWORLD).orElseThrow();
        RegistryEntry<PlacedFeature> striatedNether = placed.getOptional(AllPlacedFeatures.STRIATED_ORES_NETHER).orElseThrow();
        int index = GenerationStep.Feature.UNDERGROUND_ORES.ordinal();
        addFeature(registryManager, DimensionOptions.OVERWORLD, index, List.of(zincOverworld, striatedOverworld));
        addFeature(registryManager, DimensionOptions.NETHER, index, List.of(striatedNether));
    }

    private static void addFeature(
        DynamicRegistryManager registryManager,
        RegistryKey<DimensionOptions> options,
        int index,
        List<RegistryEntry<PlacedFeature>> entries
    ) {
        registryManager.getOrThrow(RegistryKeys.DIMENSION).get(options).chunkGenerator().getBiomeSource().getBiomes().stream()
            .map(RegistryEntry::value).map(Biome::getGenerationSettings).forEach(settings -> {
                if (!(settings.features instanceof ArrayList<RegistryEntryList<PlacedFeature>>)) {
                    settings.features = new ArrayList<>(settings.features);
                }
                List<RegistryEntryList<PlacedFeature>> features = settings.features;
                int size = features.size();
                if (size <= index) {
                    for (int i = size; i < index; i++) {
                        features.add(RegistryEntryList.of(Collections.emptyList()));
                    }
                    features.add(RegistryEntryList.of(entries));
                } else {
                    RegistryEntryList<PlacedFeature> values = features.get(index);
                    if (values != null) {
                        List<RegistryEntry<PlacedFeature>> list = new ArrayList<>(values.stream().toList());
                        list.addAll(entries);
                        values = RegistryEntryList.of(list);
                    } else {
                        values = RegistryEntryList.of(entries);
                    }
                    features.set(index, values);
                }
            });
    }

    public static void register() {
    }
}
