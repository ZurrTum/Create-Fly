package com.zurrtum.create.client.content.processing.basin;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BasinValueBox extends ValueBoxTransform.Sided {

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 12, 16.05);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return direction.getAxis().isHorizontal();
    }

}