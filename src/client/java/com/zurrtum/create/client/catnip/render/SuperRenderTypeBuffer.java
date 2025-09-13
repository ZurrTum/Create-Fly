package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public interface SuperRenderTypeBuffer extends VertexConsumerProvider {
    VertexConsumer getEarlyBuffer(RenderLayer type);

    VertexConsumer getBuffer(RenderLayer type);

    VertexConsumer getLateBuffer(RenderLayer type);

    default VertexConsumer getEarlyBuffer(BlockRenderLayer type) {
        return getEarlyBuffer(getRenderLayer(type));
    }

    default VertexConsumer getBuffer(BlockRenderLayer type) {
        return getBuffer(getRenderLayer(type));
    }

    default VertexConsumer getLateBuffer(BlockRenderLayer type) {
        return getLateBuffer(getRenderLayer(type));
    }

    default RenderLayer getRenderLayer(BlockRenderLayer type) {
        return switch (type) {
            case SOLID -> RenderLayer.getSolid();
            case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
            case CUTOUT -> RenderLayer.getCutout();
            case TRANSLUCENT -> PonderRenderTypes.translucent();
            case TRIPWIRE -> RenderLayer.getTripwire();
        };
    }

    void draw();

    void draw(RenderLayer type);
}
