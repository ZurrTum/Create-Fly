package com.zurrtum.create.client.content.kinetics.deployer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerFilterSlot extends ValueBoxTransform.Sided {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(DeployerBlock.FACING);
        Vec3 vec = VecHelper.voxelSpace(8.0f, 8.0f, 15.5f);

        vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
        vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        vec = vec.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(2 / 16.0f));

        return vec;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.getValue(DeployerBlock.FACING);
        return direction.getAxis() != facing.getAxis() && ((DeployerBlock) state.getBlock()).getRotationAxis(state) != direction.getAxis();
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Pose pose = ms.last();
        Direction facing = getSide();
        if (facing.getAxis() == Direction.Axis.Y) {
            pose.rotate(Axis.YP, Radians.horizontalPlus180(state.getValue(DeployerBlock.FACING)));
        }
        float yRadians = Radians.horizontalPlus180(facing);
        if (yRadians != 0) {
            pose.rotate(Axis.YP, yRadians);
        }
        float xRadians = facing == Direction.UP ? Radians.DEG_90 : facing == Direction.DOWN ? Radians.DEG_270 : 0;
        if (xRadians != 0) {
            pose.rotate(Axis.XP, xRadians);
        }
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

}
