package com.zurrtum.create.foundation.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface CustomAttackSoundItem {
    void playSound(
        Level world,
        Player player,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource category,
        float volume,
        float pitch
    );
}
