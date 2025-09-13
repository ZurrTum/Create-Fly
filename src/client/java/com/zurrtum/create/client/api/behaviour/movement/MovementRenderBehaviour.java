package com.zurrtum.create.client.api.behaviour.movement;

import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.Nullable;

public interface MovementRenderBehaviour {
    default void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
    }

    @Nullable
    default ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return null;
    }
}
