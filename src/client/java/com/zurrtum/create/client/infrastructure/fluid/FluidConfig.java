package com.zurrtum.create.client.infrastructure.fluid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.component.ComponentChanges;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record FluidConfig(
    Supplier<Sprite> still, Supplier<Sprite> flowing, Function<ComponentChanges, Integer> tint, Supplier<Float> fogDistance, int fogColor
) {
    private static final Map<FluidConfig, Sprite[]> CACHE = new IdentityHashMap<>();

    @SuppressWarnings("deprecation")
    public FluidConfig(Identifier still, Identifier flowing, int tint, Supplier<Float> fogDistance, int fogColor) {
        this(
            () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(still),
            () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(flowing),
            stack -> tint,
            fogDistance,
            fogColor
        );
    }

    public FluidConfig(Supplier<Sprite> still, Supplier<Sprite> flowing, Function<ComponentChanges, Integer> tint) {
        this(still, flowing, tint, () -> 0f, -1);
    }

    public Sprite[] toSprite() {
        return CACHE.computeIfAbsent(this, config -> new Sprite[]{config.still.get(), config.flowing.get()});
    }
}
