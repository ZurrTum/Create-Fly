package com.zurrtum.create.client.content.kinetics.fan;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class AirCurrentClient {
    private static boolean isClientPlayerInAirCurrent;

    private static AirCurrentSound flyingSound;

    public static void enableClientPlayerSound(Entity e, float maxVolume) {
        if (e != MinecraftClient.getInstance().getCameraEntity())
            return;

        isClientPlayerInAirCurrent = true;

        float pitch = (float) MathHelper.clamp(e.getVelocity().length() * .5f, .5f, 2f);

        if (flyingSound == null || flyingSound.isDone()) {
            flyingSound = new AirCurrentSound(SoundEvents.ITEM_ELYTRA_FLYING, pitch);
            MinecraftClient.getInstance().getSoundManager().play(flyingSound);
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
