package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface RunningEffectControlBlock {
    boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity);
}
