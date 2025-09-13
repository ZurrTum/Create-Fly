package com.zurrtum.create.foundation.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ResistanceControlBlock {
    float getResistance(BlockView world, BlockPos pos);
}
