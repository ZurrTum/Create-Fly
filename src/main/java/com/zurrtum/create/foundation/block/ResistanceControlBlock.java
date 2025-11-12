package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public interface ResistanceControlBlock {
    float getResistance(BlockGetter world, BlockPos pos);
}
