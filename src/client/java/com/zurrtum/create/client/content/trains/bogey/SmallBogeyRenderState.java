package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.UnknownNullability;

public class SmallBogeyRenderState extends StandardBogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState frame;
    public @UnknownNullability SuperByteBufferRenderState wheels;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        super.submit(matrices, queue);
        matrices.pushPose();
        matrices.scale(0.998046875f, 0.998046875f, 0.998046875f);
        frame.submit(matrices, queue);
        matrices.popPose();
        Pose pose = matrices.last();
        if (wheelRadians != 0) {
            pose.normal().rotateX(wheelRadians);
            matrices.pushPose();
            pose = matrices.last();
            pose.translate(0, 0.75f, 1);
            pose.pose().rotateX(wheelRadians);
            wheels.submit(matrices, queue);
            matrices.popPose();
            pose = matrices.last();
            pose.translate(0, 0.75f, -1);
            pose.pose().rotateX(wheelRadians);
            wheels.submit(matrices, queue);
        } else {
            pose.translate(0, 0.75f, 1);
            wheels.submit(matrices, queue);
            pose.translate(0, 0, -2);
            wheels.submit(matrices, queue);
        }
    }
}
