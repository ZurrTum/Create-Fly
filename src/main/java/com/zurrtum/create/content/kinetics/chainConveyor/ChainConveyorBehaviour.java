package com.zurrtum.create.content.kinetics.chainConveyor;

import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

public abstract class ChainConveyorBehaviour extends BlockEntityBehaviour<ChainConveyorBlockEntity> {
    public static final BehaviourType<ChainConveyorBehaviour> TYPE = new BehaviourType<>();

    public ChainConveyorBehaviour(ChainConveyorBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void blockEntityTickBoxVisuals() {
    }

    public void tickBoxVisuals() {
    }

    public void updateChainShapes() {
    }

    public void invalidate() {
    }
}
