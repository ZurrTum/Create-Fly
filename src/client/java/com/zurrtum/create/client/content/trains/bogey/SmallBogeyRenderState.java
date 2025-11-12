package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SmallBogeyRenderState extends StandardBogeyRenderState {
    public SuperByteBuffer frame;
    public SuperByteBuffer wheels;

    @Override
    public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
        super.render(matricesEntry, vertexConsumer);
        frame.scale(0.998046875f).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 0.75f, 1).rotateX(angle).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 0.75f, -1).rotateX(angle).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
    }
}
