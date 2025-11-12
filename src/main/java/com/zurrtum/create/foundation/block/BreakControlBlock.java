package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BreakControlBlock {
    boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player);
}