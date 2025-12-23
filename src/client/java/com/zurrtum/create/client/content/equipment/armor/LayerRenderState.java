package com.zurrtum.create.client.content.equipment.armor;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class LayerRenderState<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> implements SubmitNodeCollector.CustomGeometryRenderer {
    private static final PoseStack poseStack = new PoseStack();
    public M model;
    public S state;
    public int light;

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        poseStack.pushPose();
        poseStack.last().set(pose);
        model.setupAnim(state);
        model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}
