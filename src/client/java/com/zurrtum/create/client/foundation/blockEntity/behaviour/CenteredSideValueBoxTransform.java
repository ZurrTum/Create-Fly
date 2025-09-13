package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiPredicate;

public class CenteredSideValueBoxTransform extends ValueBoxTransform.Sided {

    private BiPredicate<BlockState, Direction> allowedDirections;

    public CenteredSideValueBoxTransform() {
        this((b, d) -> true);
    }

    public CenteredSideValueBoxTransform(BiPredicate<BlockState, Direction> allowedDirections) {
        this.allowedDirections = allowedDirections;
    }

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 15.5);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return allowedDirections.test(state, direction);
    }

}