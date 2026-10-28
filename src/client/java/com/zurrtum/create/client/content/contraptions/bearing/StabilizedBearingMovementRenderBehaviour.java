package com.zurrtum.create.client.content.contraptions.bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class StabilizedBearingMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new StabilizedBearingVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    @Nullable
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        PoseStack.Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        Direction facing = context.state.getValue(BlockStateProperties.FACING);
        // rotate against parent
        float angle = getCounterRotationAngle(context, facing, AnimationTickHolder.getPartialTicks());
        BlockPos pos = context.localPos;
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        Axis axis = facing.getAxis();
        SuperByteBuffer top = CachedBuffers.partial(AllPartialModels.BEARING_TOP, context.state).transform(transform)
            .translate(pos).center().rotateDegrees(angle, axis);
        if (axis.isHorizontal()) {
            top.rotateY(Radians.horizontalPlus180(facing));
        }
        SuperByteBufferRenderState renderState = top.rotateX(Radians.negatedVerticalPlus90(facing)).uncenter()
            .light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return renderState::submit;
    }

    static float getCounterRotationAngle(MovementContext context, Direction facing, float renderPartialTicks) {
        if (!context.contraption.canBeStabilized(facing, context.localPos)) {
            return 0;
        }

        float offset = 0;
        Axis axis = facing.getAxis();
        AbstractContraptionEntity entity = context.contraption.entity;

        if (entity instanceof ControlledContraptionEntity controlledCE) {
            if (context.contraption.canBeStabilized(facing, context.localPos)) {
                offset = -controlledCE.getAngle(renderPartialTicks);
            }

        } else if (entity instanceof OrientedContraptionEntity orientedCE) {
            if (axis.isVertical()) {
                offset = -orientedCE.getViewYRot(renderPartialTicks);
            } else {
                if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation().getAxis() == axis) {
                    offset = -orientedCE.getViewXRot(renderPartialTicks);
                }
            }
        }
        return offset;
    }
}
