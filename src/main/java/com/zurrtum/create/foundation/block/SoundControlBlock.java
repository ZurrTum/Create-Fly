package com.zurrtum.create.foundation.block;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface SoundControlBlock {
    BlockSoundGroup getSoundGroup(WorldView level, BlockPos pos);
}
