package com.zurrtum.create.client.infrastructure.fluid;

import net.minecraft.client.texture.Sprite;
import net.minecraft.component.ComponentChanges;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record FluidConfig(
    Supplier<Sprite> still, Supplier<Sprite> flowing, Function<ComponentChanges, Integer> tint, Supplier<Float> fogDistance, int fogColor
) {
    private static final Map<FluidConfig, Sprite[]> CACHE = new IdentityHashMap<>();

    public FluidConfig(Supplier<Sprite> still, Supplier<Sprite> flowing, Function<ComponentChanges, Integer> tint) {
        this(still, flowing, tint, () -> 0f, -1);
    }

    public Sprite[] toSprite() {
        return CACHE.computeIfAbsent(this, config -> new Sprite[]{config.still.get(), config.flowing.get()});
    }
}
