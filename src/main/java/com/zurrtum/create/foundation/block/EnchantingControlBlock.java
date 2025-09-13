package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface EnchantingControlBlock {
    BlockState getEnchantmentPowerProvider(World world, BlockPos pos);
}