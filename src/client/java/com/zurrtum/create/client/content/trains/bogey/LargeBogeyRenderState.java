package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.UnknownNullability;

public class LargeBogeyRenderState extends StandardBogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState secondaryShaft;
    public @UnknownNullability SuperByteBufferRenderState drive;
    public @UnknownNullability SuperByteBufferRenderState belt;
    public @UnknownNullability SuperByteBufferRenderState piston;
    public float pistonOffset;
    public @UnknownNullability SuperByteBufferRenderState wheels;
    public @UnknownNullability SuperByteBufferRenderState pin;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        super.submit(matrices, queue);
        matrices.pushPose();
        Pose pose = matrices.last();
        if (wheelRadians != 0) {
            pose.normal().rotateX(wheelRadians);
            matrices.pushPose();
            pose = matrices.last();
            pose.translate(0, 0.75f, 1);
            pose.pose().rotateX(wheelRadians);
            pose.translate(-0.5f, -0.5f, -0.5f);
            secondaryShaft.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            pose.translate(0, 0.75f, -1);
            pose.pose().rotateX(wheelRadians);
            pose.translate(-0.5f, -0.5f, -0.5f);
            secondaryShaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            pose.translate(-0.5f, 0.25f, 0.5f);
            secondaryShaft.submit(matrices, queue);
            pose.translate(0, 0, -2);
            secondaryShaft.submit(matrices, queue);
        }
        matrices.popPose();
        matrices.pushPose();
        matrices.scale(0.998046875f, 0.998046875f, 0.998046875f);
        drive.submit(matrices, queue);
        belt.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, 0, pistonOffset);
        piston.submit(matrices, queue);
        matrices.popPose();
        pose = matrices.last();
        pose.translate(0, 1, 0);
        if (wheelRadians != 0) {
            pose.rotate(Axis.XP, wheelRadians);
            wheels.submit(matrices, queue);
            pose.translate(0, 0.25f, 0);
            pose.rotate(Axis.XP, -wheelRadians);
        } else {
            wheels.submit(matrices, queue);
            pose.translate(0, 0.25f, 0);
        }
        pin.submit(matrices, queue);
    }
}
