package com.zurrtum.create.client.content.trains.track;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;

public abstract class TrackBlockRenderState implements OrderedRenderCommandQueue.Custom {
    public RenderLayer layer;

    public abstract void transform(MatrixStack matrices);

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
        matrices.push();
        transform(matrices);
        queue.submitCustom(matrices, layer, this);
        matrices.pop();
    }

    public void render(MatrixStack matrices, VertexConsumerProvider buffer) {
        matrices.push();
        transform(matrices);
        render(matrices.peek(), buffer.getBuffer(layer));
        matrices.pop();
    }
}
