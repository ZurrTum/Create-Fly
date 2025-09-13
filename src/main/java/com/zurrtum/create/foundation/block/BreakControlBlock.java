package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BreakControlBlock {
    boolean onDestroyedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player);
}