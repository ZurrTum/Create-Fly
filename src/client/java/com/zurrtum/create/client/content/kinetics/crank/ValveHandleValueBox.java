package com.zurrtum.create.client.content.kinetics.crank;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class ValveHandleValueBox extends ValueBoxTransform.Sided {

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return direction == state.get(ValveHandleBlock.FACING);
    }

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 4.5);
    }

    @Override
    public boolean testHit(WorldAccess level, BlockPos pos, BlockState state, Vec3d localHit) {
        Vec3d offset = getLocalOffset(state);
        if (offset == null)
            return false;
        return localHit.distanceTo(offset) < scale / 1.5f;
    }

}