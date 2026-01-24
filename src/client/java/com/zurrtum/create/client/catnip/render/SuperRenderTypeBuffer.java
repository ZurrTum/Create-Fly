package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public interface SuperRenderTypeBuffer extends MultiBufferSource {
    VertexConsumer getEarlyBuffer(RenderType type);

    VertexConsumer getBuffer(RenderType type);

    VertexConsumer getLateBuffer(RenderType type);

    default VertexConsumer getEarlyBuffer(ChunkSectionLayer type) {
        return getEarlyBuffer(getRenderLayer(type));
    }

    default VertexConsumer getBuffer(ChunkSectionLayer type) {
        return getBuffer(getRenderLayer(type));
    }

    default VertexConsumer getLateBuffer(ChunkSectionLayer type) {
        return getLateBuffer(getRenderLayer(type));
    }

    default RenderType getRenderLayer(ChunkSectionLayer type) {
        return switch (type) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    void draw();

    void draw(RenderType type);
}
