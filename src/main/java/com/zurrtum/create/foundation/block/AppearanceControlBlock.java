package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public interface AppearanceControlBlock {
    BlockState getAppearance(BlockState state, BlockRenderView level, BlockPos toPos, Direction face, BlockState reference, BlockPos fromPos);
}
