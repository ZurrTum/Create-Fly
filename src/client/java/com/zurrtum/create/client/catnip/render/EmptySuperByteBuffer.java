package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("deprecation")
class EmptySuperByteBuffer extends SuperByteBuffer {
    static final SuperByteBuffer INSTANCE = new EmptySuperByteBuffer();
    static final SuperByteBufferRenderState EMPTY_STATE = new EmptyRenderState();

    @Override
    public SuperByteBufferRenderState extractRenderState() {
        return EMPTY_STATE;
    }

    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
    }

    @Override
    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
    }

    @Override
    public void renderInto(Pose pose, VertexConsumer consumer) {
    }

    @Override
    public SuperByteBuffer reset() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public SuperByteBuffer cardinalLighting(@Nullable Level level) {
        return this;
    }

    @Override
    public SuperByteBuffer cardinalLighting(@Nullable CardinalLighting light) {
        return this;
    }

    @Override
    public SuperByteBuffer scale(float factorX, float factorY, float factorZ) {
        return this;
    }

    @Override
    public SuperByteBuffer scale(float factor) {
        return this;
    }

    @Override
    public SuperByteBuffer scaleX(float factor) {
        return this;
    }

    @Override
    public SuperByteBuffer scaleY(float factor) {
        return this;
    }

    @Override
    public SuperByteBuffer scaleZ(float factor) {
        return this;
    }

    @Override
    public SuperByteBuffer scale(Vector3fc factors) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(Quaternionfc quaternion) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(AxisAngle4f axisAngle) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, float axisX, float axisY, float axisZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Vector3fc axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Direction axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotate(float radians, Direction.Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateDegrees(float degrees, float axisX, float axisY, float axisZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateDegrees(float degrees, Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateDegrees(float degrees, Vector3fc axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateDegrees(float degrees, Direction axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateDegrees(float degrees, Direction.Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateX(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateY(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateZ(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateXDegrees(float degrees) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateYDegrees(float degrees) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateZDegrees(float degrees) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateToFace(Direction facing) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateTo(Vector3fc from, Vector3fc to) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateTo(Direction from, Direction to) {
        return this;
    }

    @Override
    public SuperByteBuffer self() {
        return this;
    }

    @Override
    public SuperByteBuffer translate(float x, float y, float z) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(double x, double y, double z) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(float v) {
        return this;
    }

    @Override
    public SuperByteBuffer translateX(float x) {
        return this;
    }

    @Override
    public SuperByteBuffer translateY(float y) {
        return this;
    }

    @Override
    public SuperByteBuffer translateZ(float z) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(Vec3i vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(Vector3ic vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(Vector3fc vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translate(Vec3 vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(float x, float y, float z) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(double x, double y, double z) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(float v) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(Vec3i vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(Vector3ic vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(Vector3fc vec) {
        return this;
    }

    @Override
    public SuperByteBuffer translateBack(Vec3 vec) {
        return this;
    }

    @Override
    public SuperByteBuffer center() {
        return this;
    }

    @Override
    public SuperByteBuffer uncenter() {
        return this;
    }

    @Override
    public SuperByteBuffer nudge(int seed) {
        return this;
    }

    @Override
    public SuperByteBuffer mulPose(Matrix4fc pose) {
        return this;
    }

    @Override
    public SuperByteBuffer mulNormal(Matrix3fc normal) {
        return this;
    }

    @Override
    public SuperByteBuffer transform(Matrix4fc pose, Matrix3fc normal) {
        return this;
    }

    @Override
    public SuperByteBuffer transform(Pose pose) {
        return this;
    }

    @Override
    public SuperByteBuffer transform(PoseStack stack) {
        return this;
    }

    @Override
    public SuperByteBuffer color(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public SuperByteBuffer color(int color) {
        return this;
    }

    @Override
    public SuperByteBuffer color(Color c) {
        return this;
    }

    @Override
    public SuperByteBuffer disableDiffuse() {
        return this;
    }

    @Override
    public SuperByteBuffer keepAlive() {
        return this;
    }

    @Override
    public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
        return this;
    }

    @Override
    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollV) {
        return this;
    }

    @Override
    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry, float scrollU, float scrollV) {
        return this;
    }

    @Override
    public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float scrollU, float scrollV, int sheetSize) {
        return this;
    }

    @Override
    public SuperByteBuffer overlay(int overlay) {
        return this;
    }

    @Override
    public SuperByteBuffer light(int packedLight) {
        return this;
    }

    @Override
    public SuperByteBuffer useLevelLight(BlockAndLightGetter level) {
        return this;
    }

    @Override
    public SuperByteBuffer useLevelLight(BlockAndLightGetter level, Matrix4f lightTransform) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(Quaternionfc quaternion, Vector3fc vec) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(Quaternionfc q) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Vector3fc axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Direction.Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCentered(float radians, Direction axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCenteredDegrees(float degrees, float axisX, float axisY, float axisZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCenteredDegrees(float degrees, Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCenteredDegrees(float degrees, Vector3fc axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCenteredDegrees(float degrees, Direction axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateCenteredDegrees(float degrees, Direction.Axis axis) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateXCentered(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateYCentered(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateZCentered(float radians) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(
        float radians,
        float axisX,
        float axisY,
        float axisZ,
        float pivotX,
        float pivotY,
        float pivotZ
    ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(float radians, Axis axis, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(float radians, Direction.Axis axis, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateAround(float radians, Direction direction, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateXAround(float radians, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateYAround(float radians, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateZAround(float radians, float pivotX, float pivotY, float pivotZ) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateXCenteredDegrees(float degrees) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateYCenteredDegrees(float degrees) {
        return this;
    }

    @Override
    public SuperByteBuffer rotateZCenteredDegrees(float degrees) {
        return this;
    }

    private static class EmptyRenderState implements SuperByteBufferRenderState {
        @Override
        public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        }

        @Override
        public void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        }

        @Override
        public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        }

        @Override
        public void renderInto(Pose pose, VertexConsumer consumer) {
        }

        @Override
        public void render(Pose pose, VertexConsumer buffer) {
        }

        @Override
        public void recycle() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }
}
