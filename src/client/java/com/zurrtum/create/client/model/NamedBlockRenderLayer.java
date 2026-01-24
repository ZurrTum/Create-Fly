package com.zurrtum.create.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public final class NamedBlockRenderLayer {
    private static final ImmutableMap<String, ChunkSectionLayer> RENDER_TYPES = Util.make(() -> {
        ImmutableMap.Builder<String, ChunkSectionLayer> builder = ImmutableMap.builder();
        builder.put("minecraft:solid", ChunkSectionLayer.SOLID);
        builder.put("minecraft:cutout", ChunkSectionLayer.CUTOUT);
        builder.put("minecraft:cutout_mipped", ChunkSectionLayer.CUTOUT);
        builder.put("minecraft:cutout_mipped_all", ChunkSectionLayer.CUTOUT);
        builder.put("minecraft:translucent", ChunkSectionLayer.TRANSLUCENT);
        return builder.build();
    });

    @Nullable
    public static ChunkSectionLayer get(String name) {
        return RENDER_TYPES.get(name);
    }
}
