package com.zurrtum.create.client.flywheel.lib.instance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.infrastructure.pose.AxisHolder;
import net.minecraft.core.Direction;
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

    public TransformedInstance mul(PoseStack.Pose other) {
        return mul(other.pose());
    }

    public TransformedInstance mul(PoseStack stack) {
        return mul(stack.last());
    }

    public TransformedInstance setTransform(Matrix4fc pose) {
        this.pose.set(pose);
        return this;
    }

    public TransformedInstance setTransform(PoseStack.Pose pose) {
        this.pose.set(pose.pose());
        return this;
    }

    public TransformedInstance setTransform(PoseStack stack) {
        return setTransform(stack.last());
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
        pose.translate(CENTER, CENTER, CENTER).rotate(radians, axisX, axisY, axisZ)
            .translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateCentered(float radians, Axis axis) {
        pose.translate(CENTER, CENTER, CENTER);
        axis.rotate(pose, radians);
        pose.translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateCentered(float radians, Direction axis) {
        pose.translate(CENTER, CENTER, CENTER);
        ((AxisHolder) (Object) axis).create$getMathAxis().rotate(pose, radians);
        pose.translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateCentered(float radians, Direction.Axis axis) {
        pose.translate(CENTER, CENTER, CENTER);
        ((AxisHolder) (Object) axis).create$getMathAxis().rotate(pose, radians);
        pose.translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateXCentered(float radians) {
        pose.translate(CENTER, CENTER, CENTER).rotateX(radians).translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateYCentered(float radians) {
        pose.translate(CENTER, CENTER, CENTER).rotateY(radians).translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotateZCentered(float radians) {
        pose.translate(CENTER, CENTER, CENTER).rotateZ(radians).translate(-CENTER, -CENTER, -CENTER);
        return this;
    }

    @Override
    public TransformedInstance rotate(float radians, float axisX, float axisY, float axisZ) {
        pose.rotate(radians, axisX, axisY, axisZ);
        return this;
    }

    @Override
    public TransformedInstance rotate(float radians, Axis axis) {
        axis.rotate(pose, radians);
        return this;
    }

    @Override
    public TransformedInstance rotate(float radians, Direction axis) {
        ((AxisHolder) (Object) axis).create$getMathAxis().rotate(pose, radians);
        return this;
    }

    @Override
    public TransformedInstance rotate(float radians, Direction.Axis axis) {
        ((AxisHolder) (Object) axis).create$getMathAxis().rotate(pose, radians);
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
