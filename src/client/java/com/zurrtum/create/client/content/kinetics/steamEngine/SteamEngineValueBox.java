package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class SteamEngineValueBox extends ValueBoxTransform.Sided {

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        Direction engineFacing = SteamEngineBlock.getFacing(state);
        if (engineFacing.getAxis() == side.getAxis())
            return false;

        float roll = 0;
        for (Pointing p : Pointing.values())
            if (p.getCombinedDirection(engineFacing) == side)
                roll = p.getXRotation();
        if (engineFacing == Direction.UP)
            roll += 180;

        boolean recessed = roll % 180 == 0;
        if (engineFacing.getAxis() == Axis.Y)
            recessed ^= state.get(SteamEngineBlock.FACING).getAxis() == Axis.X;

        return !recessed;
    }

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction side = getSide();
        Direction engineFacing = SteamEngineBlock.getFacing(state);

        float roll = 0;
        for (Pointing p : Pointing.values())
            if (p.getCombinedDirection(engineFacing) == side)
                roll = p.getXRotation();
        if (engineFacing == Direction.UP)
            roll += 180;

        float horizontalAngle = AngleHelper.horizontalAngle(engineFacing);
        float verticalAngle = AngleHelper.verticalAngle(engineFacing);
        Vec3d local = VecHelper.voxelSpace(8, 14.5, 9);

        local = VecHelper.rotateCentered(local, roll, Axis.Z);
        local = VecHelper.rotateCentered(local, horizontalAngle, Axis.Y);
        local = VecHelper.rotateCentered(local, verticalAngle, Axis.X);

        return local;
    }

    @Override
    public void rotate(WorldAccess level, BlockPos pos, BlockState state, MatrixStack ms) {
        Direction facing = SteamEngineBlock.getFacing(state);

        if (facing.getAxis() == Axis.Y) {
            super.rotate(level, pos, state, ms);
            return;
        }

        float roll = 0;
        for (Pointing p : Pointing.values())
            if (p.getCombinedDirection(facing) == getSide())
                roll = p.getXRotation();

        float yRot = AngleHelper.horizontalAngle(facing) + (facing == Direction.DOWN ? 180 : 0);
        TransformStack.of(ms).rotateYDegrees(yRot).rotateXDegrees(facing == Direction.DOWN ? -90 : 90).rotateYDegrees(roll);
    }

    @Override
    protected Vec3d getSouthLocation() {
        return Vec3d.ZERO;
    }

}
