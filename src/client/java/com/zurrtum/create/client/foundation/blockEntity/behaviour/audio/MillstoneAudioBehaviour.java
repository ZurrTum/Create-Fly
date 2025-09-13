package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.foundation.sound.SoundScapes.AmbienceGroup;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import net.minecraft.util.math.MathHelper;

public class MillstoneAudioBehaviour extends KineticAudioBehaviour<MillstoneBlockEntity> {
    public MillstoneAudioBehaviour(MillstoneBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        super.tickAudio();

        if (blockEntity.getSpeed() == 0)
            return;
        if (blockEntity.capability.getStack(0).isEmpty())
            return;

        float pitch = MathHelper.clamp((Math.abs(blockEntity.getSpeed()) / 256f) + .45f, .85f, 1f);
        SoundScapes.play(AmbienceGroup.MILLING, blockEntity.getPos(), pitch);
    }
}
