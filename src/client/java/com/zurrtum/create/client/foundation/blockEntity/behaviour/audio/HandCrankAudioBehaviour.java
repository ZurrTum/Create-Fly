package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;

public class HandCrankAudioBehaviour extends KineticAudioBehaviour<HandCrankBlockEntity> {
    public HandCrankAudioBehaviour(HandCrankBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        super.tickAudio();
        if (blockEntity.inUse > 0 && AnimationTickHolder.getTicks() % 10 == 0) {
            if (!blockEntity.getBlockState().is(AllBlocks.HAND_CRANK))
                return;
            AllSoundEvents.CRANKING.playAt(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                blockEntity.inUse / 2.5f,
                .65f + (10 - blockEntity.inUse) / 10f,
                true
            );
        }
    }
}
