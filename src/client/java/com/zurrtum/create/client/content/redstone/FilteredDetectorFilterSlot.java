package com.zurrtum.create.client.content.redstone;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class FilteredDetectorFilterSlot extends ValueBoxTransform.Sided {

    private final boolean hasSlotAtBottom;

    public FilteredDetectorFilterSlot(boolean hasSlotAtBottom) {
        this.hasSlotAtBottom = hasSlotAtBottom;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction targetDirection = DirectedDirectionalBlock.getTargetDirection(state);
        if (direction == targetDirection)
            return false;
        if (targetDirection.getOpposite() == direction)
            return true;

        if (targetDirection.getAxis() != Axis.Y)
            return direction == Direction.UP || direction == Direction.DOWN && hasSlotAtBottom;
        if (targetDirection == Direction.UP)
            direction = direction.getOpposite();
        if (!hasSlotAtBottom)
            return direction == state.get(DirectedDirectionalBlock.FACING);

        return direction.getAxis() == state.get(DirectedDirectionalBlock.FACING).rotateYClockwise().getAxis();
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        super.rotate(level, pos, state, ms);
        Direction facing = state.get(DirectedDirectionalBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return;
        if (getSide() != Direction.UP)
            return;
        TransformStack.of(ms).rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
    }

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8f, 8f, 15.5f);
    }

}
