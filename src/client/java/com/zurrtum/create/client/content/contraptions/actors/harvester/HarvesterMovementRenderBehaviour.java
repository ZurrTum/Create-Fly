package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.Nullable;

public class HarvesterMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffers
    ) {
        if (!VisualizationManager.supportsVisualization(context.world))
            HarvesterRenderer.renderInContraption(context, renderWorld, matrices, buffers);
    }

    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new HarvesterActorVisual(visualizationContext, simulationWorld, movementContext);
    }
}
