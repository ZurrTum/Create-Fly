package com.zurrtum.create.foundation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface NeighborUpdateListeningBlock {
    void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving);
}