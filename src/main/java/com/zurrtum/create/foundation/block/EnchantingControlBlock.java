package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface EnchantingControlBlock {
    BlockState getEnchantmentPowerProvider(Level world, BlockPos pos);
}