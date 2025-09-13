package com.zurrtum.create.client.flywheel.lib.transform;

import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

/**
 * A wrapper around {@link MatrixStack} that implements {@link TransformStack}.
 * <br>
 * Only one instance of this class should exist per {@link MatrixStack}.
 */
public final class PoseTransformStack implements TransformStack<PoseTransformStack> {
    private final MatrixStack stack;

    /**
     * Use {@link TransformStack#of(MatrixStack)}.
     */
    @ApiStatus.Internal
    public PoseTransformStack(MatrixStack stack) {
        this.stack = stack;
    }

    @Override
    public PoseTransformStack pushPose() {
        stack.push();
        return this;
    }

    @Override
    public PoseTransformStack popPose() {
        stack.pop();
        return this;
    }

    @Override
    public PoseTransformStack mulPose(Matrix4fc pose) {
        stack.peek().getPositionMatrix().mul(pose);
        return this;
    }

    @Override
    public PoseTransformStack mulNormal(Matrix3fc normal) {
        stack.peek().getNormalMatrix().mul(normal);
        return this;
    }

    @Override
    public PoseTransformStack rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        MatrixStack.Entry pose = stack.peek();
        pose.getPositionMatrix().rotateAround(quaternion, x, y, z);
        pose.getNormalMatrix().rotate(quaternion);
        return this;
    }

    @Override
    public PoseTransformStack translate(float x, float y, float z) {
        stack.translate(x, y, z);
        return this;
    }

    @Override
    public PoseTransformStack rotate(Quaternionfc quaternion) {
        MatrixStack.Entry pose = stack.peek();
        pose.getPositionMatrix().rotate(quaternion);
        pose.getNormalMatrix().rotate(quaternion);
        return this;
    }

    @Override
    public PoseTransformStack scale(float factorX, float factorY, float factorZ) {
        stack.scale(factorX, factorY, factorZ);
        return this;
    }

    public MatrixStack unwrap() {
        return stack;
    }
}
