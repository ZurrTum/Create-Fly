package com.zurrtum.create.client.infrastructure.pose;

import com.mojang.math.Axis;
import com.zurrtum.create.infrastructure.pose.AxisHolder;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

public interface PoseAffineExtension {
    void rotate(final Axis axis, final float angle);

    default void rotate(final Vector3fc axis, final float radians) {
        rotate(axis.x(), axis.y(), axis.z(), radians);
    }

    default void rotate(final Direction direction, final float radians) {
        rotate(((AxisHolder) (Object) direction).create$getMathAxis(), radians);
    }

    default void rotate(final Direction.Axis axis, final float radians) {
        rotate(((AxisHolder) (Object) axis).create$getMathAxis(), radians);
    }

    default void rotateCentered(final Axis axis, final float radians) {
        rotateAround(axis, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateCentered(final Direction direction, final float radians) {
        rotateAround(((AxisHolder) (Object) direction).create$getMathAxis(), radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Direction direction,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        rotateAround(((AxisHolder) (Object) direction).create$getMathAxis(), radians, pivotX, pivotY, pivotZ);
    }

    default void rotateCentered(final Direction.Axis axis, final float radians) {
        rotateAround(((AxisHolder) (Object) axis).create$getMathAxis(), radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Direction.Axis axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        rotateAround(((AxisHolder) (Object) axis).create$getMathAxis(), radians, pivotX, pivotY, pivotZ);
    }

    default void rotateXCentered(final float radians) {
        rotateXAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateEastAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateXAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateEastCentered(final float radians) {
        rotateXAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateWestAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateXAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateWestCentered(final float radians) {
        rotateXAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateYCentered(final float radians) {
        rotateYAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateUpAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateYAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateUpCentered(final float radians) {
        rotateYAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateDownAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateYAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateDownCentered(final float radians) {
        rotateYAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateZCentered(final float radians) {
        rotateZAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateSouthAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateZAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateSouthCentered(final float radians) {
        rotateZAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateNorthAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        rotateZAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateNorthCentered(final float radians) {
        rotateZAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Vector3fc axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        rotateAround(axis.x(), axis.y(), axis.z(), radians, pivotX, pivotY, pivotZ);
    }

    default void rotateCentered(final Vector3fc axis, final float radians) {
        rotateAround(axis.x(), axis.y(), axis.z(), radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateCentered(final float x, final float y, final float z, final float radians) {
        rotateAround(x, y, z, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Axis axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateXAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateYAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateZAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotate(final float x, final float y, final float z, final float radians) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateAround(
        final float x,
        final float y,
        final float z,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateXYZ(final float xRadians, final float yRadians, final float zRadians) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateYXZ(final float yRadians, final float xRadians, final float zRadians) {
        throw new RuntimeException("Implemented via Mixin");
    }

    default void rotateZYX(final float zRadians, final float yRadians, final float xRadians) {
        throw new RuntimeException("Implemented via Mixin");
    }
}
