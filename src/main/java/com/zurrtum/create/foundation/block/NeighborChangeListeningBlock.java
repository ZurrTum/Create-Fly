package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface NeighborChangeListeningBlock {
    void onNeighborChange(BlockState state, WorldView world, BlockPos pos, BlockPos neighbor);
}
