package com.zurrtum.create.client.infrastructure.fluid;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;

import java.util.function.Function;
import java.util.function.Supplier;

public record FluidConfig(
    Supplier<TextureAtlasSprite> still, Supplier<TextureAtlasSprite> flowing, Function<DataComponentPatch, Integer> tint, Supplier<Float> fogDistance,
    int fogColor
) {
    public FluidConfig(Supplier<TextureAtlasSprite> still, Supplier<TextureAtlasSprite> flowing, Function<DataComponentPatch, Integer> tint) {
        this(still, flowing, tint, () -> 0f, -1);
    }
}
