package com.zurrtum.create.client.foundation.sound;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.sound.SoundScapes.AmbienceGroup;
import com.zurrtum.create.client.foundation.sound.SoundScapes.PitchGroup;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SoundScape {
    private final float pitch;
    private final AmbienceGroup group;
    private final PitchGroup pitchGroup;
    List<ContinuousSound> continuous;
    List<RepeatingSound> repeating;
    private Vec3d meanPos;

    public SoundScape(float pitch, AmbienceGroup group) {
        this.pitchGroup = SoundScapes.getGroupFromPitch(pitch);
        this.pitch = pitch;
        this.group = group;
        continuous = new ArrayList<>();
        repeating = new ArrayList<>();
    }

    public SoundScape continuous(SoundEvent sound, float relativeVolume, float relativePitch) {
        return add(new ContinuousSound(sound, this, pitch * relativePitch, relativeVolume));
    }

    public SoundScape repeating(SoundEvent sound, float relativeVolume, float relativePitch, int delay) {
        return add(new RepeatingSound(sound, this, pitch * relativePitch, relativeVolume, delay));
    }

    public SoundScape add(ContinuousSound continuousSound) {
        continuous.add(continuousSound);
        return this;
    }

    public SoundScape add(RepeatingSound repeatingSound) {
        repeating.add(repeatingSound);
        return this;
    }

    public void play() {
        continuous.forEach(MinecraftClient.getInstance().getSoundManager()::play);
    }

    public void tick() {
        if (AnimationTickHolder.getTicks() % SoundScapes.UPDATE_INTERVAL == 0)
            meanPos = null;
        repeating.forEach(RepeatingSound::tick);
    }

    public void remove() {
        continuous.forEach(ContinuousSound::remove);
    }

    public Vec3d getMeanPos() {
        return meanPos == null ? meanPos = determineMeanPos() : meanPos;
    }

    private Vec3d determineMeanPos() {
        meanPos = Vec3d.ZERO;
        int amount = 0;
        for (BlockPos blockPos : SoundScapes.getAllLocations(group, pitchGroup)) {
            meanPos = meanPos.add(VecHelper.getCenterOf(blockPos));
            amount++;
        }
        if (amount == 0)
            return meanPos;
        return meanPos.multiply(1f / amount);
    }

    public float getVolume() {
        Entity renderViewEntity = MinecraftClient.getInstance().cameraEntity;
        float distanceMultiplier = 0;
        if (renderViewEntity != null) {
            double distanceTo = renderViewEntity.getEntityPos().distanceTo(getMeanPos());
            distanceMultiplier = (float) MathHelper.lerp(distanceTo / SoundScapes.MAX_AMBIENT_SOURCE_DISTANCE, 2, 0);
        }
        int soundCount = SoundScapes.getSoundCount(group, pitchGroup);
        float max = AllConfigs.client().ambientVolumeCap.getF();
        float argMax = (float) SoundScapes.SOUND_VOLUME_ARG_MAX;
        return MathHelper.clamp(soundCount / (argMax * 10f), 0.025f, max) * distanceMultiplier;
    }

}