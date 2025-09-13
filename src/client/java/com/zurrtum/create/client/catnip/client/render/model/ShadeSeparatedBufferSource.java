package com.zurrtum.create.client.catnip.client.render.model;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumer;

public interface ShadeSeparatedBufferSource {
    VertexConsumer getBuffer(BlockRenderLayer chunkRenderType, boolean shade);
}
