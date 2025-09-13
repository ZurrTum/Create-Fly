package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

import java.util.List;

public class LayeredOreConfiguration implements FeatureConfig {
    public static final Codec<LayeredOreConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.list(LayerPattern.CODEC).fieldOf("layer_patterns").forGetter(config -> config.layerPatterns),
            Codec.intRange(0, 64).fieldOf("size").forGetter(config -> config.size),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(config -> config.discardChanceOnAirExposure)
        ).apply(instance, LayeredOreConfiguration::new);
    });

    public final List<LayerPattern> layerPatterns;
    public final int size;
    public final float discardChanceOnAirExposure;

    public LayeredOreConfiguration(List<LayerPattern> layerPatterns, int size, float discardChanceOnAirExposure) {
        this.layerPatterns = layerPatterns;
        this.size = size;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
    }
}
