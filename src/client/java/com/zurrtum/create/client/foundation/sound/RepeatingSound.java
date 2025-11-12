package com.zurrtum.create.client.foundation.sound;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class RepeatingSound {

    private final SoundEvent event;
    private final float sharedPitch;
    private final int repeatDelay;
    private final SoundScape scape;
    private final float relativeVolume;

    public RepeatingSound(SoundEvent event, SoundScape scape, float sharedPitch, float relativeVolume, int repeatDelay) {
        this.event = event;
        this.scape = scape;
        this.sharedPitch = sharedPitch;
        this.relativeVolume = relativeVolume;
        this.repeatDelay = Math.max(1, repeatDelay);
    }

    public void tick() {
        if (AnimationTickHolder.getTicks() % repeatDelay != 0)
            return;

        ClientLevel world = Minecraft.getInstance().level;
        Vec3 meanPos = scape.getMeanPos();

        world.playLocalSound(meanPos.x, meanPos.y, meanPos.z, event, SoundSource.AMBIENT, scape.getVolume() * relativeVolume, sharedPitch, true);
    }

}