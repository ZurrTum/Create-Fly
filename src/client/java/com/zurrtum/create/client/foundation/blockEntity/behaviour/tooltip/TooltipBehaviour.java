package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

public abstract class TooltipBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<TooltipBehaviour<?>> TYPE = new BehaviourType<>();

    public TooltipBehaviour(T be) {
        super(be);
    }

    @Override
    public void tick() {
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
