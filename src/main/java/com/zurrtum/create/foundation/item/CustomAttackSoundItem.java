package com.zurrtum.create.foundation.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public interface CustomAttackSoundItem {
    void playSound(
        World world,
        PlayerEntity player,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundCategory category,
        float volume,
        float pitch
    );
}
