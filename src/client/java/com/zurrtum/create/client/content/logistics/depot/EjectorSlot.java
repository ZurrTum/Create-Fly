package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class EjectorSlot extends ValueBoxTransform.Sided {
    private final EjectorBlockEntity blockEntity;

    public EjectorSlot(EjectorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        if (direction != Direction.UP)
            return super.getLocalOffset(state);
        return new Vec3d(.5, 10.5 / 16f, .5).add(VecHelper.rotate(VecHelper.voxelSpace(0, 0, -5), angle(state), Axis.Y));
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        if (direction != Direction.UP) {
            super.rotate(state, ms);
            return;
        }
        TransformStack.of(ms).rotateYDegrees(angle(state)).rotateXDegrees(90);
    }

    protected float angle(BlockState state) {
        return state.isOf(AllBlocks.WEIGHTED_EJECTOR) ? AngleHelper.horizontalAngle(state.get(EjectorBlock.HORIZONTAL_FACING)) : 0;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return direction.getAxis() == state.get(EjectorBlock.HORIZONTAL_FACING)
            .getAxis() || direction == Direction.UP && blockEntity.getState() != EjectorBlockEntity.State.CHARGED;
    }

    @Override
    protected Vec3d getSouthLocation() {
        return direction == Direction.UP ? Vec3d.ZERO : VecHelper.voxelSpace(8, 6, 15.5);
    }

}
