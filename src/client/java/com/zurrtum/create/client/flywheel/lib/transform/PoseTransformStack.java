package com.zurrtum.create.client.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

/**
 * A wrapper around {@link PoseStack} that implements {@link TransformStack}.
 * <br>
 * Only one instance of this class should exist per {@link PoseStack}.
 */
public final class PoseTransformStack implements TransformStack<PoseTransformStack> {
    private final PoseStack stack;

    /**
     * Use {@link TransformStack#of(PoseStack)}.
     */
    @ApiStatus.Internal
    public PoseTransformStack(PoseStack stack) {
        this.stack = stack;
    }

    @Override
    public PoseTransformStack pushPose() {
        stack.pushPose();
        return this;
    }

    @Override
    public PoseTransformStack popPose() {
        stack.popPose();
        return this;
    }

    @Override
    public PoseTransformStack mulPose(Matrix4fc pose) {
        stack.last().pose().mul(pose);
        return this;
    }

    @Override
    public PoseTransformStack mulNormal(Matrix3fc normal) {
        stack.last().normal().mul(normal);
        return this;
    }

    @Override
    public PoseTransformStack rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        stack.rotateAround(quaternion, x, y, z);
        return this;
    }

    @Override
    public PoseTransformStack translate(float x, float y, float z) {
        stack.translate(x, y, z);
        return this;
    }

    @Override
    public PoseTransformStack rotate(Quaternionfc quaternion) {
        stack.rotate(quaternion);
        return this;
    }

    @Override
    public PoseTransformStack scale(float factorX, float factorY, float factorZ) {
        stack.scale(factorX, factorY, factorZ);
        return this;
    }

    @Override
    public PoseTransformStack rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            stack.rotateCentered(axisX, axisY, axisZ, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateCentered(float radians, Axis axis) {
        if (radians != 0) {
            stack.rotateCentered(axis, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateCentered(float radians, Direction.Axis axis) {
        if (radians != 0) {
            stack.rotateCentered(axis, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateCentered(float radians, Direction direction) {
        if (radians != 0) {
            stack.rotateCentered(direction, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateXCentered(float radians) {
        if (radians != 0) {
            stack.rotateXCentered(radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateYCentered(float radians) {
        if (radians != 0) {
            stack.rotateYCentered(radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateZCentered(float radians) {
        if (radians != 0) {
            stack.rotateZCentered(radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotate(float radians, float axisX, float axisY, float axisZ) {
        if (radians != 0) {
            stack.rotate(axisX, axisY, axisZ, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotate(float radians, Axis axis) {
        if (radians != 0) {
            stack.rotate(axis, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotate(float radians, Direction direction) {
        if (radians != 0) {
            stack.rotate(direction, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotate(float radians, Direction.Axis axis) {
        if (radians != 0) {
            stack.rotate(axis, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateX(float radians) {
        if (radians != 0) {
            stack.rotate(Axis.XP, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateY(float radians) {
        if (radians != 0) {
            stack.rotate(Axis.YP, radians);
        }
        return this;
    }

    @Override
    public PoseTransformStack rotateZ(float radians) {
        if (radians != 0) {
            stack.rotate(Axis.ZP, radians);
        }
        return this;
    }

    public PoseStack unwrap() {
        return stack;
    }
}
