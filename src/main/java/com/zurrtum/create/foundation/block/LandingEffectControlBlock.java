package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface LandingEffectControlBlock {
    boolean addLandingEffects(BlockState state, ServerLevel world, BlockPos pos, LivingEntity entity, double distance);
}
