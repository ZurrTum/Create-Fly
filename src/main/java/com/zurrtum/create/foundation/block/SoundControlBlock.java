package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;

public interface SoundControlBlock {
    SoundType getSoundGroup(LevelReader level, BlockPos pos);
}
