package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public interface BufferEmitter extends BufferEmitterOutput {
    VertexConsumer getBuffer(@Nullable Direction shadeDirectionOverride, ChunkSectionLayer layer);
}
