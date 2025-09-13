package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.block.Block;
import net.minecraft.util.math.MathHelper;

public class KineticAudioBehaviour<T extends KineticBlockEntity> extends AudioBehaviour<T> {
    public KineticAudioBehaviour(T be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        float componentSpeed = Math.abs(blockEntity.getSpeed());
        if (componentSpeed == 0)
            return;
        float pitch = MathHelper.clamp((componentSpeed / 256f) + .45f, .85f, 1f);

        if (blockEntity.isNoisy()) {
            SoundScapes.play(SoundScapes.AmbienceGroup.KINETIC, blockEntity.getPos(), pitch);
        }

        Block block = blockEntity.getCachedState().getBlock();
        if (ICogWheel.isSmallCog(block) || ICogWheel.isLargeCog(block) || block instanceof GearboxBlock) {
            SoundScapes.play(SoundScapes.AmbienceGroup.COG, blockEntity.getPos(), pitch);
        }
    }
}
