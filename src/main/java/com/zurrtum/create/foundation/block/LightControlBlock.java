package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public interface LightControlBlock {
    int getLuminance(BlockGetter world, BlockPos pos);
}