package com.zurrtum.create.content.fluids.tank;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.entity.BlockEntity;

// The fluid level needs to be ticked to animate smoothly
public class FluidTankMovementBehavior extends MovementBehaviour {
    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClient) {
            BlockEntity be = context.contraption.presentBlockEntities.get(context.localPos);
            if (be instanceof FluidTankBlockEntity tank) {
                tank.getFluidLevel().tickChaser();
            }
        }
    }
}