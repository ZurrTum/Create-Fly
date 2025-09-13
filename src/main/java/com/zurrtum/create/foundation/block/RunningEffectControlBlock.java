package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface RunningEffectControlBlock {
    boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity);
}
