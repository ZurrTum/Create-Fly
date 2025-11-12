package com.zurrtum.create.client.catnip.client.render.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface ShadeSeparatedBufferSource {
    VertexConsumer getBuffer(ChunkSectionLayer chunkRenderType, boolean shade);
}
