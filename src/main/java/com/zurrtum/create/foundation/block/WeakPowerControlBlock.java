package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;

public interface WeakPowerControlBlock {
    boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side);
}
