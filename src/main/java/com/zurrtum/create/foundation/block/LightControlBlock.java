package com.zurrtum.create.foundation.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface LightControlBlock {
    int getLuminance(BlockView world, BlockPos pos);
}