package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.math.VecHelper;
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
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class RollerMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new RollerActorVisual(visualizationContext, simulationWorld, movementContext);
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
        BlockPos pos = context.localPos;
        RollerMovementRenderState state = new RollerMovementRenderState();
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float speed = !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()) ?
            context.getAnimationSpeed() : -context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        float wheelRadians = Radians.horizontal(facing);
        float frameRadians = Radians.horizontalPlus180(facing);
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        float xRadians = Mth.DEG_TO_RAD * -(time * speed % 360);
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        SuperByteBuffer frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState).transform(transform)
            .translate(pos);
        SuperByteBuffer wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, blockState);
        SuperByteBuffer.copyTransform(frame, wheel);
        state.frame = frame.rotateYCentered(frameRadians).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        state.wheel = wheel.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(17 / 16.0f))
            .rotateYCentered(wheelRadians).rotateX(xRadians).translate(0, -0.5, 0.5).rotateY(Radians.DEG_90)
            .light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return state;
    }

    public static class RollerMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState wheel;
        public @UnknownNullability SuperByteBufferRenderState frame;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            frame.submit(matrices, queue);
            matrices.pushPose();
            matrices.translate(0, -0.25, 0);
            wheel.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
