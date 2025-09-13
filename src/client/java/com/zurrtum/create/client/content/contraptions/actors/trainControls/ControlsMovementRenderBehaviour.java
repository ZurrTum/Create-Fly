package com.zurrtum.create.client.content.contraptions.actors.trainControls;

import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour.LeverAngles;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.Direction;

import java.util.Collection;

public class ControlsMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        if (!(context.temporaryData instanceof LeverAngles angles))
            return;

        AbstractContraptionEntity entity = context.contraption.entity;
        if (!(entity instanceof CarriageContraptionEntity cce))
            return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        Direction initialOrientation = cce.getInitialOrientation().rotateYCounterclockwise();
        boolean inverted = false;
        if (info != null && info.state().contains(ControlsBlock.FACING))
            inverted = !info.state().get(ControlsBlock.FACING).equals(initialOrientation);

        if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null && ControlsHandler.getControlsPos()
            .equals(context.localPos)) {
            Collection<Integer> pressed = ControlsHandler.currentlyPressed;
            angles.equipAnimation.chase(1, .2f, Chaser.EXP);
            angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
            float f = cce.movingBackwards ^ inverted ? -1 : 1;
            angles.speed.chase(Math.min(context.motion.length(), 0.5f) * f, 0.2f, Chaser.EXP);

        } else {
            angles.equipAnimation.chase(0, .2f, Chaser.EXP);
            angles.steering.chase(0, 0, Chaser.EXP);
            angles.speed.chase(0, 0, Chaser.EXP);
        }

        float pt = AnimationTickHolder.getPartialTicks(context.world);
        ControlsRenderer.render(
            context,
            renderWorld,
            matrices,
            buffer,
            angles.equipAnimation.getValue(pt),
            angles.speed.getValue(pt),
            angles.steering.getValue(pt)
        );
    }

}
