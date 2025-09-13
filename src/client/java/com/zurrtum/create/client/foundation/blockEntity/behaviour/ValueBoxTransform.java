package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public abstract class ValueBoxTransform {

    protected float scale = getScale();

    public abstract Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state);

    public abstract void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms);

    public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
        Vec3d offset = getLocalOffset(level, pos, state);
        if (offset == null)
            return false;
        return localHit.distanceTo(offset) < scale / 2;
    }

    public void transform(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Vec3d position = getLocalOffset(level, pos, state);
        if (position == null)
            return;
        ms.translate(position.x, position.y, position.z);
        rotate(level, pos, state, ms);
        ms.scale(scale, scale, scale);
    }

    public boolean shouldRender(WorldAccess level, BlockPos pos, BlockState state) {
        return !state.isAir() && getLocalOffset(level, pos, state) != null;
    }

    public int getOverrideColor() {
        return -1;
    }

    protected Vec3d rotateHorizontally(BlockState state, Vec3d vec) {
        float yRot = 0;
        if (state.contains(Properties.FACING))
            yRot = AngleHelper.horizontalAngle(state.get(Properties.FACING));
        if (state.contains(Properties.HORIZONTAL_FACING))
            yRot = AngleHelper.horizontalAngle(state.get(Properties.HORIZONTAL_FACING));
        return VecHelper.rotateCentered(vec, yRot, Axis.Y);
    }

    public float getScale() {
        return .5f;
    }

    public float getFontScale() {
        return 1 / 64f;
    }

    public static abstract class Dual extends ValueBoxTransform {

        protected boolean first;

        public Dual(boolean first) {
            this.first = first;
        }

        public boolean isFirst() {
            return first;
        }

        public static Pair<ValueBoxTransform, ValueBoxTransform> makeSlots(Function<Boolean, ? extends Dual> factory) {
            return Pair.of(factory.apply(true), factory.apply(false));
        }

        @Override
        public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
            Vec3d offset = getLocalOffset(level, pos, state);
            if (offset == null)
                return false;
            return localHit.distanceTo(offset) < scale / 3.5f;
        }

    }

    public static abstract class Sided extends ValueBoxTransform {

        protected Direction direction = Direction.UP;

        public Sided fromSide(Direction direction) {
            this.direction = direction;
            return this;
        }

        @Override
        public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
            Vec3d location = getSouthLocation();
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Axis.X);
            return location;
        }

        protected abstract Vec3d getSouthLocation();

        @Override
        public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
            TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(xRot);
        }

        @Override
        public boolean shouldRender(WorldAccess level, BlockPos pos, BlockState state) {
            return super.shouldRender(level, pos, state) && isSideActive(state, getSide());
        }

        @Override
        public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
            return isSideActive(state, getSide()) && super.testHit(level, pos, state, localHit);
        }

        protected boolean isSideActive(BlockState state, Direction direction) {
            return true;
        }

        public Direction getSide() {
            return direction;
        }

    }

}
