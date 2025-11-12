package com.zurrtum.create.client.infrastructure.fluid;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;

public record FluidConfig(
    Supplier<TextureAtlasSprite> still, Supplier<TextureAtlasSprite> flowing, Function<DataComponentPatch, Integer> tint, Supplier<Float> fogDistance,
    int fogColor
) {
    private static final Map<FluidConfig, TextureAtlasSprite[]> CACHE = new IdentityHashMap<>();

    public FluidConfig(Supplier<TextureAtlasSprite> still, Supplier<TextureAtlasSprite> flowing, Function<DataComponentPatch, Integer> tint) {
        this(still, flowing, tint, () -> 0f, -1);
    }

    public TextureAtlasSprite[] toSprite() {
        return CACHE.computeIfAbsent(this, config -> new TextureAtlasSprite[]{config.still.get(), config.flowing.get()});
    }
}
