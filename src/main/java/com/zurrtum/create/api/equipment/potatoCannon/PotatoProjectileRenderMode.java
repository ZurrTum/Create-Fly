package com.zurrtum.create.api.equipment.potatoCannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;

import java.util.function.Function;

// TODO: 1.21.7 - Move into api package
public interface PotatoProjectileRenderMode {
    Codec<PotatoProjectileRenderMode> CODEC = CreateRegistries.POTATO_PROJECTILE_RENDER_MODE.getCodec()
        .dispatch(PotatoProjectileRenderMode::codec, Function.identity());

    MapCodec<? extends PotatoProjectileRenderMode> codec();
}
