package com.zurrtum.create.client.content.equipment.armor;


import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

public class LayerRenderState<S extends BipedEntityRenderState, M extends BipedEntityModel<? super S>> implements OrderedRenderCommandQueue.Custom {
    private static final MatrixStack poseStack = new MatrixStack();
    public M model;
    public S state;
    public int light;

    @Override
    public void render(MatrixStack.Entry pose, VertexConsumer vertexConsumer) {
        poseStack.push();
        poseStack.peek().copy(pose);
        model.setAngles(state);
        model.render(poseStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);
        poseStack.pop();
    }
}
