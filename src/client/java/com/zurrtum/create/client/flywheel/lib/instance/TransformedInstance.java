package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

public class TransformedInstance extends ColoredLitOverlayInstance implements Affine<TransformedInstance> {
    public final Matrix4f pose = new Matrix4f();

    public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    @Override
    public TransformedInstance translate(float x, float y, float z) {
        pose.translate(x, y, z);
        return this;
    }

    @Override
    public TransformedInstance rotate(Quaternionfc quaternion) {
        pose.rotate(quaternion);
        return this;
    }

    @Override
    public TransformedInstance scale(float x, float y, float z) {
        pose.scale(x, y, z);
        return this;
    }

    public TransformedInstance mul(Matrix4fc other) {
        pose.mul(other);
        return this;
    }

    public TransformedInstance mul(MatrixStack.Entry other) {
        return mul(other.getPositionMatrix());
    }

    public TransformedInstance mul(MatrixStack stack) {
        return mul(stack.peek());
    }

    public TransformedInstance setTransform(Matrix4fc pose) {
        this.pose.set(pose);
        return this;
    }

    public TransformedInstance setTransform(MatrixStack.Entry pose) {
        this.pose.set(pose.getPositionMatrix());
        return this;
    }

    public TransformedInstance setTransform(MatrixStack stack) {
        return setTransform(stack.peek());
    }

    public TransformedInstance setIdentityTransform() {
        pose.identity();
        return this;
    }

    /**
     * Sets the transform matrices to be all zeros.
     *
     * <p>
     * This will allow the GPU to quickly discard all geometry for this instance, effectively "turning it off".
     * </p>
     */
    public TransformedInstance setZeroTransform() {
        pose.zero();
        return this;
    }

    @Override
    public TransformedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        pose.rotateAround(quaternion, x, y, z);
        return this;
    }

    @Override
    public TransformedInstance rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER).rotate(radians, axisX, axisY, axisZ)
            .translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateXCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER).rotateX(radians).translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateYCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER).rotateY(radians).translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateZCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER).rotateZ(radians).translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotate(float radians, float axisX, float axisY, float axisZ) {
        pose.rotate(radians, axisX, axisY, axisZ);
        return this;
    }

    @Override
    public TransformedInstance rotate(AxisAngle4f axisAngle) {
        pose.rotate(axisAngle);
        return this;
    }

    @Override
    public TransformedInstance rotateX(float radians) {
        pose.rotateX(radians);
        return this;
    }

    @Override
    public TransformedInstance rotateY(float radians) {
        pose.rotateY(radians);
        return this;
    }

    @Override
    public TransformedInstance rotateZ(float radians) {
        pose.rotateZ(radians);
        return this;
    }
}
