package com.zurrtum.create.client.content.kinetics.fan;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class AirCurrentSound extends MovingSoundInstance {

    private float pitch;

    protected AirCurrentSound(SoundEvent p_i46532_1_, float pitch) {
        super(p_i46532_1_, SoundCategory.BLOCKS, SoundInstance.createRandom());
        this.pitch = pitch;
        volume = 0.01f;
        repeat = true;
        repeatDelay = 0;
        relative = true;
    }

    @Override
    public void tick() {
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void fadeIn(float maxVolume) {
        volume = Math.min(maxVolume, volume + .05f);
    }

    public void fadeOut() {
        volume = Math.max(0, volume - .05f);
    }

    public boolean isFaded() {
        return volume == 0;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    public void stopSound() {
        setDone();
    }

}
