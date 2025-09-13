package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.Nullable;

public class DeployerMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffers
    ) {
        if (!VisualizationManager.supportsVisualization(context.world))
            DeployerRenderer.renderInContraption(context, renderWorld, matrices, buffers);
    }

    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new DeployerActorVisual(visualizationContext, simulationWorld, movementContext);
    }
}
