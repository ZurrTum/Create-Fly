package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

public class StandardBogeyRenderState implements BogeyRenderState, OrderedRenderCommandQueue.Custom {
    public RenderLayer layer;
    public SuperByteBuffer shaft;
    public float angle;
    public int light;
    public double offset;

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.translate(0, offset, 0);
        queue.submitCustom(matrices, layer, this);
    }

    @Override
    public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
        shaft.translate(-.5f, .25f, 0).center().rotateZ(angle).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
            .renderInto(matricesEntry, vertexConsumer);
        shaft.translate(-.5f, .25f, -1).center().rotateZ(angle).uncenter().light(light).overlay(OverlayTexture.DEFAULT_UV)
            .renderInto(matricesEntry, vertexConsumer);
    }
}
