package com.zurrtum.create.client.content.logistics.funnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        Direction funnelFacing = FunnelBlock.getFunnelFacing(state);
        float stateAngle = AngleHelper.horizontalAngle(funnelFacing);

        if (state.getBlock() instanceof BeltFunnelBlock) {
            return switch (state.getValue(BeltFunnelBlock.SHAPE)) {
                case EXTENDED ->
                    VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 13), stateAngle, Direction.Axis.Y);
                case PULLING, PUSHING ->
                    VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.0f, 8.675f), horizontalAngle, Direction.Axis.Y);
                default ->
                    VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f), horizontalAngle, Direction.Axis.Y);
            };
        }

        if (!funnelFacing.getAxis().isHorizontal()) {
            Vec3 southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 14 : 2, 15.5f);
            return VecHelper.rotateCentered(southLocation, horizontalAngle, Direction.Axis.Y);
        }

        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.2, 8.55f), horizontalAngle, Direction.Axis.Y);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = FunnelBlock.getFunnelFacing(state);

        if (facing.getAxis().isVertical()) {
            super.rotate(state, ms);
            return;
        }

        boolean isBeltFunnel = state.getBlock() instanceof BeltFunnelBlock;
        if (isBeltFunnel && state.getValue(BeltFunnelBlock.SHAPE) != Shape.EXTENDED) {
            Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
            super.rotate(state, ms);
            if (shape == Shape.PULLING || shape == Shape.PUSHING) {
                ms.rotate(Axis.XP, Radians.DEG_337P5);
            }
            return;
        }

        if (state.getBlock() instanceof FunnelBlock) {
            super.rotate(state, ms);
            ms.rotate(Axis.XP, Radians.DEG_337P5);
            return;
        }
        float yRadians, xRadians;
        if (facing == Direction.DOWN) {
            yRadians = Radians.horizontalPlus180(facing);
            xRadians = Radians.DEG_270;
        } else {
            yRadians = Radians.horizontal(facing);
            xRadians = Radians.DEG_90;
        }
        Pose pose = ms.last();
        if (yRadians != 0) {
            pose.rotate(Axis.YP, yRadians);
        }
        pose.rotate(Axis.XP, xRadians);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = FunnelBlock.getFunnelFacing(state);

        if (facing == null) {
            return false;
        }
        if (facing.getAxis().isVertical()) {
            return direction.getAxis().isHorizontal();
        }
        if (state.getBlock() instanceof BeltFunnelBlock && state.getValue(BeltFunnelBlock.SHAPE) == Shape.EXTENDED) {
            return direction == Direction.UP;
        }
        return direction == facing;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

}
