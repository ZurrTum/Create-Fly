package com.zurrtum.create.client.content.logistics.chute;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class SmartChuteFilterSlotPositioning extends ValueBoxTransform.Sided {

    @Override
    public Vec3d getLocalOffset(WorldAccess level, BlockPos pos, BlockState state) {
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        Vec3d southLocation = VecHelper.voxelSpace(8, 11, 15.5f);
        return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return direction.getAxis().isHorizontal();
    }

    @Override
    protected Vec3d getSouthLocation() {
        return Vec3d.ZERO;
    }

}
