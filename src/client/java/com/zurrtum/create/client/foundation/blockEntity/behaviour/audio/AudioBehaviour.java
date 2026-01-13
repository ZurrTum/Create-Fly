package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

abstract class AudioBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<AudioBehaviour<?>> TYPE = new BehaviourType<>();

    public AudioBehaviour(T be) {
        super(be);
    }

    @Override
    public void tick() {
        if (blockEntity.getWorld().isClient()) {
            tickAudio();
        }
    }

    public abstract void tickAudio();

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
