package com.zurrtum.create.client.flywheel.lib.transform;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface Rotate<Self extends Rotate<Self>> {
    Self rotate(Quaternionfc quaternion);

    default Self rotate(AxisAngle4f axisAngle) {
        return rotate(new Quaternionf(axisAngle));
    }

    default Self rotate(float radians, float axisX, float axisY, float axisZ) {
        if (radians == 0) {
            return self();
        }
        return rotate(new Quaternionf().setAngleAxis(radians, axisX, axisY, axisZ));

    }

    default Self rotate(float radians, RotationAxis axis) {
        if (radians == 0) {
            return self();
        }
        return rotate(axis.rotation(radians));
    }

    default Self rotate(float radians, Vector3fc axis) {
        return rotate(radians, axis.x(), axis.y(), axis.z());
    }

    default Self rotate(float radians, Direction axis) {
        return rotate(radians, axis.getOffsetX(), axis.getOffsetY(), axis.getOffsetZ());
    }

    default Self rotate(float radians, Direction.Axis axis) {
        return rotate(radians, Direction.from(axis, Direction.AxisDirection.POSITIVE));
    }

    default Self rotateDegrees(float degrees, float axisX, float axisY, float axisZ) {
        return rotate(MathHelper.RADIANS_PER_DEGREE * degrees, axisX, axisY, axisZ);
    }

    default Self rotateDegrees(float degrees, RotationAxis axis) {
        return rotate(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateDegrees(float degrees, Vector3fc axis) {
        return rotate(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateDegrees(float degrees, Direction axis) {
        return rotate(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateDegrees(float degrees, Direction.Axis axis) {
        return rotate(MathHelper.RADIANS_PER_DEGREE * degrees, axis);
    }

    default Self rotateX(float radians) {
        return rotate(radians, RotationAxis.POSITIVE_X);
    }

    default Self rotateY(float radians) {
        return rotate(radians, RotationAxis.POSITIVE_Y);
    }

    default Self rotateZ(float radians) {
        return rotate(radians, RotationAxis.POSITIVE_Z);
    }

    default Self rotateXDegrees(float degrees) {
        return rotateX(MathHelper.RADIANS_PER_DEGREE * degrees);
    }

    default Self rotateYDegrees(float degrees) {
        return rotateY(MathHelper.RADIANS_PER_DEGREE * degrees);
    }

    default Self rotateZDegrees(float degrees) {
        return rotateZ(MathHelper.RADIANS_PER_DEGREE * degrees);
    }

    default Self rotateToFace(Direction facing) {
        return switch (facing) {
            case DOWN -> rotateXDegrees(-90);
            case UP -> rotateXDegrees(90);
            case NORTH -> self();
            case SOUTH -> rotateYDegrees(180);
            case WEST -> rotateYDegrees(90);
            case EAST -> rotateYDegrees(270);
        };
    }

    default Self rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        return rotate(new Quaternionf().rotationTo(fromX, fromY, fromZ, toX, toY, toZ));
    }

    default Self rotateTo(Vector3fc from, Vector3fc to) {
        return rotateTo(from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
    }

    default Self rotateTo(Direction from, Direction to) {
        return rotateTo(from.getOffsetX(), from.getOffsetY(), from.getOffsetZ(), to.getOffsetX(), to.getOffsetY(), to.getOffsetZ());
    }

    @SuppressWarnings("unchecked")
    default Self self() {
        return (Self) this;
    }
}
