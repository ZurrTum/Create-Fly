package com.zurrtum.create.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public final class NamedBlockRenderLayer {
    private static final ImmutableMap<String, BlockRenderLayer> RENDER_TYPES = Util.make(() -> {
        ImmutableMap.Builder<String, BlockRenderLayer> builder = ImmutableMap.builder();
        builder.put("minecraft:solid", BlockRenderLayer.SOLID);
        builder.put("minecraft:cutout", BlockRenderLayer.CUTOUT);
        builder.put("minecraft:cutout_mipped", BlockRenderLayer.CUTOUT_MIPPED);
        builder.put("minecraft:cutout_mipped_all", BlockRenderLayer.CUTOUT_MIPPED);
        builder.put("minecraft:translucent", BlockRenderLayer.TRANSLUCENT);
        builder.put("minecraft:tripwire", BlockRenderLayer.TRIPWIRE);
        return builder.build();
    });

    @Nullable
    public static BlockRenderLayer get(String name) {
        return RENDER_TYPES.get(name);
    }
}
