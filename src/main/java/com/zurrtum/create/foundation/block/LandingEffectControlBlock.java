package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface LandingEffectControlBlock {
    boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos, LivingEntity entity, double distance);
}
