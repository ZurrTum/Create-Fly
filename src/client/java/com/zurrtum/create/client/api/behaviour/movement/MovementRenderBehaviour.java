package com.zurrtum.create.client.api.behaviour.movement;

import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public interface MovementRenderBehaviour {
    default MovementRenderState getRenderState(
        Vec3d camera,
        TextRenderer textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        return null;
    }

    @Nullable
    default ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return null;
    }
}
