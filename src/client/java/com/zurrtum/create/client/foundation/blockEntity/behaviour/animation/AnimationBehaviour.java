package com.zurrtum.create.client.foundation.blockEntity.behaviour.animation;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

public abstract class AnimationBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<AnimationBehaviour<?>> TYPE = new BehaviourType<>();

    public AnimationBehaviour(T be) {
        super(be);
    }

    @Override
    public void tick() {
        if (blockEntity.getWorld().isClient()) {
            tickAnimation();
        }
    }

    public abstract void tickAnimation();

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
