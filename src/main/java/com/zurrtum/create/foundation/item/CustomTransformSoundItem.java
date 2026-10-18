package com.zurrtum.create.foundation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface CustomTransformSoundItem {
    void playSound(
        Level level,
        Player except,
        BlockPos pos,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch
    );
}
