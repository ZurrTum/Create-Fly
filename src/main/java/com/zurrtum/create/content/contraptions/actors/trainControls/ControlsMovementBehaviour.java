package com.zurrtum.create.content.contraptions.actors.trainControls;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ControlsMovementBehaviour extends MovementBehaviour {
    // TODO: rendering the levers should be specific to Carriage Contraptions -
    public static class LeverAngles {
        public LerpedFloat steering = LerpedFloat.linear();
        public LerpedFloat speed = LerpedFloat.linear();
        public LerpedFloat equipAnimation = LerpedFloat.linear();
    }

    @Override
    @Nullable
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public void stopMoving(MovementContext context) {
        context.contraption.entity.stopControlling(context.localPos);
        super.stopMoving(context);
    }

    @Override
    public void tick(MovementContext context) {
        super.tick(context);
        if (!context.world.isClientSide())
            return;
        if (!(context.temporaryData instanceof LeverAngles))
            context.temporaryData = new LeverAngles();
        LeverAngles angles = (LeverAngles) context.temporaryData;
        angles.steering.tickChaser();
        angles.speed.tickChaser();
        angles.equipAnimation.tickChaser();
    }
}
