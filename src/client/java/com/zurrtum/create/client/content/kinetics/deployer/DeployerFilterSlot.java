package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class DeployerFilterSlot extends ValueBoxTransform.Sided {

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction facing = state.get(DeployerBlock.FACING);
        Vec3d vec = VecHelper.voxelSpace(8f, 8f, 15.5f);

        vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Axis.Y);
        vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Axis.X);
        vec = vec.subtract(Vec3d.of(facing.getVector()).multiply(2 / 16f));

        return vec;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.get(DeployerBlock.FACING);
        if (direction.getAxis() == facing.getAxis())
            return false;
        if (((DeployerBlock) state.getBlock()).getRotationAxis(state) == direction.getAxis())
            return false;
        return true;
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Direction facing = getSide();
        float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
        float yRot = AngleHelper.horizontalAngle(facing) + 180;

        if (facing.getAxis() == Axis.Y)
            TransformStack.of(ms).rotateYDegrees(180 + AngleHelper.horizontalAngle(state.get(DeployerBlock.FACING)));

        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(xRot);
    }

    @Override
    protected Vec3d getSouthLocation() {
        return Vec3d.ZERO;
    }

}
