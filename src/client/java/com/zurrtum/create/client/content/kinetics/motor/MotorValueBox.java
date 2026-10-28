package com.zurrtum.create.client.content.kinetics.motor;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MotorValueBox extends ValueBoxTransform.Sided {
    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 12.5);
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(CreativeMotorBlock.FACING);
        return super.getLocalOffset(state).add(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(-1 / 16.0f));
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        super.rotate(state, ms);
        Direction facing = state.getValue(CreativeMotorBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y) {
            return;
        }
        if (getSide() != Direction.UP) {
            return;
        }
        ms.rotate(Axis.ZP, Radians.negatedHorizontalPlus180(facing));
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.getValue(CreativeMotorBlock.FACING);
        if (facing.getAxis() != Direction.Axis.Y && direction == Direction.DOWN) {
            return false;
        }
        return direction.getAxis() != facing.getAxis();
    }
}
