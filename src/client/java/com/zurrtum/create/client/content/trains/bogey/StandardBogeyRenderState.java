package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.UnknownNullability;

public class StandardBogeyRenderState implements BogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState shaft;
    public float wheelRadians;
    public double offset;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        matrices.translate(0, offset, 0);
        matrices.pushPose();
        Pose pose = matrices.last();
        if (wheelRadians != 0) {
            pose.normal().rotateZ(wheelRadians);
            matrices.pushPose();
            pose = matrices.last();
            pose.translate(0, 0.75f, 0.5f);
            pose.pose().rotateZ(wheelRadians);
            pose.translate(-0.5f, -0.5f, -0.5f);
            shaft.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            pose.translate(0, 0.75f, -0.5f);
            pose.pose().rotateZ(wheelRadians);
            pose.translate(-0.5f, -0.5f, -0.5f);
            shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            pose.translate(-0.5f, 0.25f, 0);
            shaft.submit(matrices, queue);
            pose.translate(0, 0, -1);
            shaft.submit(matrices, queue);
        }
        matrices.popPose();
    }
}
