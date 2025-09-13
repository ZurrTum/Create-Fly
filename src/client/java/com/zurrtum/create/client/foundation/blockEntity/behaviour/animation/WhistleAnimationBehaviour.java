package com.zurrtum.create.client.foundation.blockEntity.behaviour.animation;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;

public class WhistleAnimationBehaviour extends AnimationBehaviour<WhistleBlockEntity> {
    public LerpedFloat animation = LerpedFloat.linear();

    public WhistleAnimationBehaviour(WhistleBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAnimation() {
        FluidTankBlockEntity tank = blockEntity.getTank();
        boolean powered = blockEntity.isPowered() && (tank != null && tank.boiler.isActive() && (tank.boiler.passiveHeat || tank.boiler.activeHeat > 0) || blockEntity.isVirtual());
        animation.chase(powered ? 1 : 0, powered ? .5f : .4f, powered ? LerpedFloat.Chaser.EXP : LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();
    }
}
