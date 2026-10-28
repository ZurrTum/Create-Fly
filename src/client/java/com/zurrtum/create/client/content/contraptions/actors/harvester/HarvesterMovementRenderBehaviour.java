package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class HarvesterMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new HarvesterActorVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    @Nullable
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int light = LightCoordsUtil.getLightCoords(renderWorld, context.localPos);
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        float speed = !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()) ?
            context.getAnimationSpeed() : 0;
        if (context.contraption.stalled) {
            speed = 0;
        }
        float yRadians = Radians.horizontal(facing);
        float xRadians = Mth.DEG_TO_RAD * -(time * speed % 360);
        SuperByteBufferRenderState renderState = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState)
            .transform(transform).translate(context.localPos).rotateYCentered(yRadians)
            .rotateXAround(xRadians, 0, 0.375f, 0.5625f).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        return renderState::submit;
    }
}
