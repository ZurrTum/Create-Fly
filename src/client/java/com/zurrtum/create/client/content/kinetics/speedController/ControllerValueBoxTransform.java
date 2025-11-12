package com.zurrtum.create.client.content.kinetics.speedController;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8, 11f, 15.5f);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        if (direction.getAxis().isVertical())
            return false;
        return state.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
    }

}