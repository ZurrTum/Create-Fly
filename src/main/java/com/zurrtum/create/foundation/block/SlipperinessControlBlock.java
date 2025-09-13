package com.zurrtum.create.foundation.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface SlipperinessControlBlock {
    float getSlipperiness(WorldView world, BlockPos pos);
}
