package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface AppearanceControlBlock {
    BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos toPos, Direction face, BlockState reference, BlockPos fromPos);
}
