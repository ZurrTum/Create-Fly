package com.zurrtum.create.client.content.logistics.funnel;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.funnel.AbstractFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        Direction funnelFacing = FunnelBlock.getFunnelFacing(state);
        float stateAngle = AngleHelper.horizontalAngle(funnelFacing);

        if (state.getBlock() instanceof BeltFunnelBlock) {
            switch (state.get(BeltFunnelBlock.SHAPE)) {

                case EXTENDED:
                    return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 13), stateAngle, Axis.Y);
                case PULLING:
                case PUSHING:
                    return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.0f, 8.675f), horizontalAngle, Axis.Y);
                default:
                case RETRACTED:
                    return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f), horizontalAngle, Axis.Y);
            }
        }

        if (!funnelFacing.getAxis().isHorizontal()) {
            Vec3d southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 14 : 2, 15.5f);
            return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
        }

        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.2, 8.55f), horizontalAngle, Axis.Y);
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Direction facing = FunnelBlock.getFunnelFacing(state);

        if (facing.getAxis().isVertical()) {
            super.rotate(level, pos, state, ms);
            return;
        }

        boolean isBeltFunnel = state.getBlock() instanceof BeltFunnelBlock;
        if (isBeltFunnel && state.get(BeltFunnelBlock.SHAPE) != Shape.EXTENDED) {
            Shape shape = state.get(BeltFunnelBlock.SHAPE);
            super.rotate(level, pos, state, ms);
            if (shape == Shape.PULLING || shape == Shape.PUSHING)
                TransformStack.of(ms).rotateXDegrees(-22.5f);
            return;
        }

        if (state.getBlock() instanceof FunnelBlock) {
            super.rotate(level, pos, state, ms);
            TransformStack.of(ms).rotateXDegrees(-22.5f);
            return;
        }

        float yRot = AngleHelper.horizontalAngle(AbstractFunnelBlock.getFunnelFacing(state)) + (facing == Direction.DOWN ? 180 : 0);
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(facing == Direction.DOWN ? -90 : 90);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = FunnelBlock.getFunnelFacing(state);

        if (facing == null)
            return false;
        if (facing.getAxis().isVertical())
            return direction.getAxis().isHorizontal();
        if (state.getBlock() instanceof BeltFunnelBlock && state.get(BeltFunnelBlock.SHAPE) == Shape.EXTENDED)
            return direction == Direction.UP;
        return direction == facing;
    }

    @Override
    protected Vec3d getSouthLocation() {
        return Vec3d.ZERO;
    }

}
