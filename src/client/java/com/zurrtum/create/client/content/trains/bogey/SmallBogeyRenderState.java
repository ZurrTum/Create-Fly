package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class SmallBogeyRenderState extends StandardBogeyRenderState {
    public SuperByteBuffer frame;
    public SuperByteBuffer wheels;

    @Override
    public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
        super.render(matricesEntry, vertexConsumer);
        frame.scale(0.998046875f).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 0.75f, 1).rotateX(angle).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        wheels.translate(0, 0.75f, -1).rotateX(angle).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
    }
}
