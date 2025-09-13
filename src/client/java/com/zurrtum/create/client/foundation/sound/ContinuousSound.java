package com.zurrtum.create.client.foundation.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class ContinuousSound extends MovingSoundInstance {

    private final float sharedPitch;
    private final SoundScape scape;
    private final float relativeVolume;

    protected ContinuousSound(SoundEvent event, SoundScape scape, float sharedPitch, float relativeVolume) {
        super(event, SoundCategory.AMBIENT, SoundInstance.createRandom());
        this.scape = scape;
        this.sharedPitch = sharedPitch;
        this.relativeVolume = relativeVolume;
        this.repeat = true;
        this.repeatDelay = 0;
        this.relative = false;
    }

    public void remove() {
        setDone();
    }

    @Override
    public float getVolume() {
        return scape.getVolume() * relativeVolume;
    }

    @Override
    public float getPitch() {
        return sharedPitch;
    }

    @Override
    public double getX() {
        return scape.getMeanPos().x;
    }

    @Override
    public double getY() {
        return scape.getMeanPos().y;
    }

    @Override
    public double getZ() {
        return scape.getMeanPos().z;
    }

    @Override
    public void tick() {
    }

}
