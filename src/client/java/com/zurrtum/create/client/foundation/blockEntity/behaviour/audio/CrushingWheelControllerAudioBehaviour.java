package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.foundation.sound.SoundScapes.AmbienceGroup;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.util.math.MathHelper;

public class CrushingWheelControllerAudioBehaviour extends AudioBehaviour<CrushingWheelControllerBlockEntity> {
    public CrushingWheelControllerAudioBehaviour(CrushingWheelControllerBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        if (!blockEntity.isOccupied() || blockEntity.crushingspeed == 0)
            return;
        float pitch = MathHelper.clamp((blockEntity.crushingspeed / 256f) + .45f, .85f, 1f);
        if (blockEntity.entityUUID == null && blockEntity.inventory.getStack(0).isEmpty())
            return;
        SoundScapes.play(AmbienceGroup.CRUSHING, blockEntity.getPos(), pitch);
    }
}
