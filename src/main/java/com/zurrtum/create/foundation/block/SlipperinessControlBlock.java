package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public interface SlipperinessControlBlock {
    float getSlipperiness(LevelReader world, BlockPos pos);
}
