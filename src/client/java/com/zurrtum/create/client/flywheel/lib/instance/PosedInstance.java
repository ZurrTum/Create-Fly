package com.zurrtum.create.client.flywheel.lib.instance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.transform.Transform;
import com.zurrtum.create.infrastructure.pose.AxisHolder;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.*;

import java.lang.Math;

public class PosedInstance extends ColoredLitOverlayInstance implements Transform<PosedInstance> {
    public final Matrix4f pose = new Matrix4f();
    public final Matrix3f normal = new Matrix3f();

    public PosedInstance(InstanceType<? extends PosedInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    @Override
    public PosedInstance mulPose(Matrix4fc pose) {
        this.pose.mul(pose);
        return this;
    }

    @Override
    public PosedInstance mulNormal(Matrix3fc normal) {
        this.normal.mul(normal);
        return this;
    }

    @Override
    public PosedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        pose.rotateAround(quaternion, x, y, z);
        normal.rotate(quaternion);
        return this;
    }

    @Override
    public PosedInstance translate(float x, float y, float z) {
        pose.translate(x, y, z);
        return this;
    }

    @Override
    public PosedInstance rotate(Quaternionfc quaternion) {
        pose.rotate(quaternion);
        normal.rotate(quaternion);
        return this;
    }

    @Override
    public PosedInstance scale(float x, float y, float z) {
        pose.scale(x, y, z);

        if (x == y && y == z) {
            if (x < 0.0f) {
                normal.scale(-1.0f);
            }

            return this;
        }

        float invX = 1.0f / x;
        float invY = 1.0f / y;
        float invZ = 1.0f / z;
        float f = Mth.fastInvCubeRoot(Math.abs(invX * invY * invZ));
        normal.scale(f * invX, f * invY, f * invZ);
        return this;
    }

    @Override
    public PosedInstance rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.rotate(radians, axisX, axisY, axisZ);
            normal.rotate(radians, axisX, axisY, axisZ);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateCentered(float radians, Axis axis) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateCentered(float radians, Direction.Axis directionAxis) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            Axis axis = ((AxisHolder) (Object) directionAxis).create$getMathAxis();
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateCentered(float radians, Direction direction) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            Axis axis = ((AxisHolder) (Object) direction).create$getMathAxis();
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateXCentered(float radians) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.rotateX(radians);
            normal.rotateX(radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateYCentered(float radians) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.rotateY(radians);
            normal.rotateY(radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotateZCentered(float radians) {
        if (radians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.rotateZ(radians);
            normal.rotateZ(radians);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        return this;
    }

    @Override
    public PosedInstance rotate(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            pose.rotate(radians, axisX, axisY, axisZ);
            normal.rotate(radians, axisX, axisY, axisZ);
        }
        return this;
    }

    @Override
    public PosedInstance rotate(float radians, Axis axis) {
        if (radians != 0) {
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
        }
        return this;
    }

    @Override
    public PosedInstance rotate(float radians, Direction direction) {
        if (radians != 0) {
            Axis axis = ((AxisHolder) (Object) direction).create$getMathAxis();
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
        }
        return this;
    }

    @Override
    public PosedInstance rotate(float radians, Direction.Axis directionAxis) {
        if (radians != 0) {
            Axis axis = ((AxisHolder) (Object) directionAxis).create$getMathAxis();
            axis.rotate(pose, radians);
            axis.rotate(normal, radians);
        }
        return this;
    }

    @Override
    public PosedInstance rotateX(float radians) {
        if (radians != 0) {
            pose.rotateX(radians);
            normal.rotateX(radians);
        }
        return this;
    }

    @Override
    public PosedInstance rotateY(float radians) {
        if (radians != 0) {
            pose.rotateY(radians);
            normal.rotateY(radians);
        }
        return this;
    }

    @Override
    public PosedInstance rotateZ(float radians) {
        if (radians != 0) {
            pose.rotateZ(radians);
            normal.rotateZ(radians);
        }
        return this;
    }

    public PosedInstance setTransform(Matrix4fc pose, Matrix3fc normal) {
        this.pose.set(pose);
        this.normal.set(normal);
        return this;
    }

    public PosedInstance setTransform(PoseStack.Pose pose) {
        this.pose.set(pose.pose());
        normal.set(pose.normal());
        return this;
    }

    public PosedInstance setTransform(PoseStack stack) {
        return setTransform(stack.last());
    }

    public PosedInstance setIdentityTransform() {
        pose.identity();
        normal.identity();
        return this;
    }

    /**
     * Sets the transform matrices to be all zeros.
     *
     * <p>
     * This will allow the GPU to quickly discard all geometry for this instance, effectively "turning it off".
     * </p>
     */
    public PosedInstance setZeroTransform() {
        pose.zero();
        normal.zero();
        return this;
    }
}
