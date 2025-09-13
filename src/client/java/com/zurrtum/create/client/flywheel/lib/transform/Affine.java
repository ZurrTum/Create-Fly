package com.zurrtum.create.client.flywheel.lib.transform;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface Affine<Self extends Affine<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
    default Self rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        return translate(x, y, z).rotate(quaternion).translateBack(x, y, z);
    }

    default Self rotateAround(Quaternionfc quaternion, Vector3fc vec) {
        return rotateAround(quaternion, vec.x(), vec.y(), vec.z());
    }

    default Self rotateCentered(Quaternionfc q) {
        return rotateAround(q, CENTER, CENTER, CENTER);
    }

    default Self rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        if (radians == 0) {
            return self();
        }
        return rotateCentered(new Quaternionf().setAngleAxis(radians, axisX, axisY, axisZ));
    }

    default Self rotateCentered(float radians, RotationAxis axis) {
        if (radians == 0) {
            return self();
        }
        return rotateCentered(axis.rotation(radians));
    }

    default Self rotateCentered(float radians, Vector3fc axis) {
        return rotateCentered(radians, axis.x(), axis.y(), axis.z());
    }

    default Self rotateCentered(float radians, Direction.Axis axis) {
        return rotateCentered(radians, Direction.from(axis, Direction.AxisDirection.POSITIVE));
    }

    default Self rotateCentered(float radians, Direction axis) {
        return rotateCentered(radians, axis.getOffsetX(), axis.getOffsetY(), axis.getOffsetZ());
    }

    default Self rotateCenteredDegrees(float degrees, float axisX, float axisY, float axisZ) {
        return rotateCentered(MathHelper.RADIANS_PER_DEGREE * degrees, axisX, axisY, axisZ);
    }

    default Self rotateCenteredDegrees(float degrees, RotationAxis axis) {
        return rotateCentered(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateCenteredDegrees(float degrees, Vector3fc axis) {
        return rotateCentered(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateCenteredDegrees(float degrees, Direction axis) {
        return rotateCentered(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateCenteredDegrees(float degrees, Direction.Axis axis) {
        return rotateCentered(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateXCentered(float radians) {
        return rotateCentered(radians, RotationAxis.POSITIVE_X);
    }

    default Self rotateYCentered(float radians) {
        return rotateCentered(radians, RotationAxis.POSITIVE_Y);
    }

    default Self rotateZCentered(float radians) {
        return rotateCentered(radians, RotationAxis.POSITIVE_Z);
    }

    default Self rotateXCenteredDegrees(float degrees) {
        return rotateXCentered(MathHelper.RADIANS_PER_DEGREE * degrees);
    }

    default Self rotateYCenteredDegrees(float degrees) {
        return rotateYCentered(MathHelper.RADIANS_PER_DEGREE * degrees);
    }

    default Self rotateZCenteredDegrees(float degrees) {
        return rotateZCentered(MathHelper.RADIANS_PER_DEGREE * degrees);
    }
}
