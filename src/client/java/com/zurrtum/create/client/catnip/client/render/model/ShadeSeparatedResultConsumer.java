package com.zurrtum.create.client.catnip.client.render.model;

import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface ShadeSeparatedResultConsumer {
    void accept(ChunkSectionLayer renderType, boolean shaded, MeshData data);
}
