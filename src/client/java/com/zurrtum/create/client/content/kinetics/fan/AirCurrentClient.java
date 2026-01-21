package com.zurrtum.create.client.content.kinetics.fan;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class AirCurrentClient {
    private static boolean isClientPlayerInAirCurrent;

    private static @Nullable AirCurrentSound flyingSound;

    public static void enableClientPlayerSound(Entity e, float maxVolume) {
        if (e != Minecraft.getInstance().getCameraEntity())
            return;

        isClientPlayerInAirCurrent = true;

        float pitch = (float) Mth.clamp(e.getDeltaMovement().length() * .5f, .5f, 2f);

        if (flyingSound == null || flyingSound.isStopped()) {
            flyingSound = new AirCurrentSound(SoundEvents.ELYTRA_FLYING, pitch);
            Minecraft.getInstance().getSoundManager().play(flyingSound);
        }
        flyingSound.setPitch(pitch);
        flyingSound.fadeIn(maxVolume);
    }

    public static void tickClientPlayerSounds() {
        if (!isClientPlayerInAirCurrent && flyingSound != null)
            if (flyingSound.isFaded())
                flyingSound.stopSound();
            else
                flyingSound.fadeOut();
        isClientPlayerInAirCurrent = false;
    }
}
