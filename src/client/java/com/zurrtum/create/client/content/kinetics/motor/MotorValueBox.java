package com.zurrtum.create.client.content.kinetics.motor;


import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class MotorValueBox extends ValueBoxTransform.Sided {
    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 12.5);
    }

    @Override
    public Vec3d getLocalOffset(BlockState state) {
        Direction facing = state.get(CreativeMotorBlock.FACING);
        return super.getLocalOffset(state).add(Vec3d.of(facing.getVector()).multiply(-1 / 16f));
    }

    @Override
    public void rotate(BlockState state, MatrixStack ms) {
        super.rotate(state, ms);
        Direction facing = state.get(CreativeMotorBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y)
            return;
        if (getSide() != Direction.UP)
            return;
        TransformStack.of(ms).rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.get(CreativeMotorBlock.FACING);
        if (facing.getAxis() != Direction.Axis.Y && direction == Direction.DOWN)
            return false;
        return direction.getAxis() != facing.getAxis();
    }
}
