package com.zurrtum.create.client.content.contraptions.bearing;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class StabilizedBearingMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        if (VisualizationManager.supportsVisualization(context.world))
            return;

        Direction facing = context.state.get(Properties.FACING);
        PartialModel top = AllPartialModels.BEARING_TOP;
        SuperByteBuffer superBuffer = CachedBuffers.partial(top, context.state);
        float renderPartialTicks = AnimationTickHolder.getPartialTicks();

        // rotate to match blockstate
        Quaternionf orientation = BearingVisual.getBlockStateOrientation(facing);

        // rotate against parent
        float angle = getCounterRotationAngle(context, facing, renderPartialTicks) * facing.getDirection().offset();

        Quaternionf rotation = RotationAxis.of(facing.getUnitVector()).rotationDegrees(angle);

        rotation.mul(orientation);

        orientation = rotation;

        superBuffer.transform(matrices.getModel());
        superBuffer.rotateCentered(orientation);

        // render
        superBuffer.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderLayer.getSolid()));
    }

    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new StabilizedBearingVisual(visualizationContext, simulationWorld, movementContext);
    }

    static float getCounterRotationAngle(MovementContext context, Direction facing, float renderPartialTicks) {
        if (!context.contraption.canBeStabilized(facing, context.localPos))
            return 0;

        float offset = 0;
        Direction.Axis axis = facing.getAxis();
        AbstractContraptionEntity entity = context.contraption.entity;

        if (entity instanceof ControlledContraptionEntity controlledCE) {
            if (context.contraption.canBeStabilized(facing, context.localPos))
                offset = -controlledCE.getAngle(renderPartialTicks);

        } else if (entity instanceof OrientedContraptionEntity orientedCE) {
            if (axis.isVertical())
                offset = -orientedCE.getViewYRot(renderPartialTicks);
            else {
                if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation().getAxis() == axis)
                    offset = -orientedCE.getViewXRot(renderPartialTicks);
            }
        }
        return offset;
    }
}
