package com.zurrtum.create.client.infrastructure.pose;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

public interface PoseStackAffineExtension {
    Pose last();

    default void rotate(final Vector3fc axis, final float radians) {
        last().rotate(axis.x(), axis.y(), axis.z(), radians);
    }

    default void rotate(final float x, final float y, final float z, final float radians) {
        last().rotate(x, y, z, radians);
    }

    default void rotate(final Direction direction, final float radians) {
        last().rotate(direction, radians);
    }

    default void rotate(final Direction.Axis axis, final float radians) {
        last().rotate(axis, radians);
    }

    default void rotateCentered(final Axis axis, final float radians) {
        last().rotateAround(axis, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateCentered(final Direction direction, final float radians) {
        last().rotateAround(direction, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Direction direction,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        last().rotateAround(direction, radians, pivotX, pivotY, pivotZ);
    }

    default void rotateCentered(final Direction.Axis axis, final float radians) {
        last().rotateAround(axis, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Direction.Axis axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        last().rotateAround(axis, radians, pivotX, pivotY, pivotZ);
    }

    default void rotateXAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateXAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateXCentered(final float radians) {
        last().rotateXAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateEastAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateXAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateEastCentered(final float radians) {
        last().rotateXAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateWestAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateXAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateWestCentered(final float radians) {
        last().rotateXAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateYAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateYAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateYCentered(final float radians) {
        last().rotateYAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateUpAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateYAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateUpCentered(final float radians) {
        last().rotateYAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateDownAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateYAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateDownCentered(final float radians) {
        last().rotateYAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateZAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateZAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateZCentered(final float radians) {
        last().rotateZAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateSouthAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateZAround(radians, pivotX, pivotY, pivotZ);
    }

    default void rotateSouthCentered(final float radians) {
        last().rotateZAround(radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateNorthAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        last().rotateZAround(-radians, pivotX, pivotY, pivotZ);
    }

    default void rotateNorthCentered(final float radians) {
        last().rotateZAround(-radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateAround(
        final Vector3fc axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        last().rotateAround(axis.x(), axis.y(), axis.z(), radians, pivotX, pivotY, pivotZ);
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
        last().rotateAround(x, y, z, radians, pivotX, pivotY, pivotZ);
    }

    default void rotateCentered(final Vector3fc axis, final float radians) {
        last().rotateAround(axis.x(), axis.y(), axis.z(), radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateCentered(final float x, final float y, final float z, final float radians) {
        last().rotateAround(x, y, z, radians, 0.5f, 0.5f, 0.5f);
    }

    default void rotateXYZ(final float xRadians, final float yRadians, final float zRadians) {
        last().rotateXYZ(xRadians, yRadians, zRadians);
    }

    default void rotateYXZ(final float yRadians, final float xRadians, final float zRadians) {
        last().rotateYXZ(yRadians, xRadians, zRadians);
    }

    default void rotateZYX(final float zRadians, final float yRadians, final float xRadians) {
        last().rotateZYX(zRadians, yRadians, xRadians);
    }
}
