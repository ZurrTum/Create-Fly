package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class StandardBogeyRenderState implements BogeyRenderState, SubmitNodeCollector.CustomGeometryRenderer {
    public RenderType layer;
    public SuperByteBuffer shaft;
    public float angle;
    public int light;
    public double offset;

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector queue) {
        matrices.translate(0, offset, 0);
        queue.submitCustomGeometry(matrices, layer, this);
    }

    @Override
    public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
        shaft.translate(-.5f, .25f, 0).center().rotateZ(angle).uncenter().light(light).overlay(OverlayTexture.NO_OVERLAY)
            .renderInto(matricesEntry, vertexConsumer);
        shaft.translate(-.5f, .25f, -1).center().rotateZ(angle).uncenter().light(light).overlay(OverlayTexture.NO_OVERLAY)
            .renderInto(matricesEntry, vertexConsumer);
    }
}
