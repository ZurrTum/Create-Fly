package com.zurrtum.create.client.content.redstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FilteredDetectorFilterSlot extends ValueBoxTransform.Sided {

    private final boolean hasSlotAtBottom;

    public FilteredDetectorFilterSlot(boolean hasSlotAtBottom) {
        this.hasSlotAtBottom = hasSlotAtBottom;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction targetDirection = DirectedDirectionalBlock.getTargetDirection(state);
        if (direction == targetDirection) {
            return false;
        }
        if (targetDirection.getOpposite() == direction) {
            return true;
        }

        if (targetDirection.getAxis() != Direction.Axis.Y) {
            return direction == Direction.UP || direction == Direction.DOWN && hasSlotAtBottom;
        }
        if (targetDirection == Direction.UP) {
            direction = direction.getOpposite();
        }
        if (!hasSlotAtBottom) {
            return direction == state.getValue(DirectedDirectionalBlock.FACING);
        }

        return direction.getAxis() == state.getValue(DirectedDirectionalBlock.FACING).getClockWise().getAxis();
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        super.rotate(state, ms);
        Direction facing = state.getValue(DirectedDirectionalBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y) {
            return;
        }
        if (getSide() != Direction.UP) {
            return;
        }
        ms.rotate(Axis.ZP, Radians.negatedHorizontalPlus180(facing));
    }

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8.0f, 8.0f, 15.5f);
    }

}
