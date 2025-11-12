package com.zurrtum.create.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.Nullable;

public final class NamedBlockRenderLayer {
    private static final ImmutableMap<String, ChunkSectionLayer> RENDER_TYPES = Util.make(() -> {
        ImmutableMap.Builder<String, ChunkSectionLayer> builder = ImmutableMap.builder();
        builder.put("minecraft:solid", ChunkSectionLayer.SOLID);
        builder.put("minecraft:cutout", ChunkSectionLayer.CUTOUT);
        builder.put("minecraft:cutout_mipped", ChunkSectionLayer.CUTOUT_MIPPED);
        builder.put("minecraft:cutout_mipped_all", ChunkSectionLayer.CUTOUT_MIPPED);
        builder.put("minecraft:translucent", ChunkSectionLayer.TRANSLUCENT);
        builder.put("minecraft:tripwire", ChunkSectionLayer.TRIPWIRE);
        return builder.build();
    });

    @Nullable
    public static ChunkSectionLayer get(String name) {
        return RENDER_TYPES.get(name);
    }
}
