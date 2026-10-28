package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.infrastructure.pose.PoseAffineExtension;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Pose.class)
public abstract class PoseMixin implements PoseAffineExtension {
    @Shadow
    @Final
    private Matrix4f pose;

    @Shadow
    @Final
    private Matrix3f normal;

    @Shadow
    public abstract void rotate(Axis axis, float angle);

    @Override
    public void rotateAround(
        Axis axis,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        Matrix4f pose = this.pose;
        pose.translate(pivotX, pivotY, pivotZ);
        axis.rotate(pose, radians);
        axis.rotate(normal, radians);
        pose.translate(-pivotX, -pivotY, -pivotZ);
    }

    @Override
    public void rotateXAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        Matrix4f pose = this.pose;
        pose.translate(pivotX, pivotY, pivotZ);
        pose.rotateX(radians);
        normal.rotateX(radians);
        pose.translate(-pivotX, -pivotY, -pivotZ);
    }

    @Override
    public void rotateYAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        Matrix4f pose = this.pose;
        pose.translate(pivotX, pivotY, pivotZ);
        pose.rotateY(radians);
        normal.rotateY(radians);
        pose.translate(-pivotX, -pivotY, -pivotZ);
    }

    @Override
    public void rotateZAround(final float radians, final float pivotX, final float pivotY, final float pivotZ) {
        Matrix4f pose = this.pose;
        pose.translate(pivotX, pivotY, pivotZ);
        pose.rotateZ(radians);
        normal.rotateZ(radians);
        pose.translate(-pivotX, -pivotY, -pivotZ);
    }

    @Override
    public void rotate(final float x, final float y, final float z, final float radians) {
        pose.rotate(radians, x, y, z);
        normal.rotate(radians, x, y, z);
    }

    @Override
    public void rotateAround(
        final float x,
        final float y,
        final float z,
        final float radians,
        final float pivotX,
        final float pivotY,
        final float pivotZ
    ) {
        Matrix4f pose = this.pose;
        pose.translate(pivotX, pivotY, pivotZ);
        pose.rotate(radians, x, y, z);
        normal.rotate(radians, x, y, z);
        pose.translate(-pivotX, -pivotY, -pivotZ);
    }

    @Override
    public void rotateXYZ(final float xRadians, final float yRadians, final float zRadians) {
        pose.rotateXYZ(xRadians, yRadians, zRadians);
        normal.rotateXYZ(xRadians, yRadians, zRadians);
    }

    @Override
    public void rotateYXZ(final float yRadians, final float xRadians, final float zRadians) {
        pose.rotateYXZ(yRadians, xRadians, zRadians);
        normal.rotateYXZ(yRadians, xRadians, zRadians);
    }

    @Override
    public void rotateZYX(final float zRadians, final float yRadians, final float xRadians) {
        pose.rotateZYX(zRadians, yRadians, xRadians);
        normal.rotateZYX(zRadians, yRadians, xRadians);
    }
}
