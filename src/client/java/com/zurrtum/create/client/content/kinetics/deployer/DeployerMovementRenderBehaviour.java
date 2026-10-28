package com.zurrtum.create.client.content.kinetics.deployer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
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
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new DeployerActorVisual(visualizationContext, simulationWorld, movementContext);
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
        BlockPos pos = context.localPos;
        DeployerMovementRenderState state = new DeployerMovementRenderState();
        BlockState blockState = context.state;
        Mode mode = context.blockEntityData.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);
        float speed = context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * 0.5f) * 0.25f + 0.25f;
        } else {
            Vec3 center = VecHelper.getCenterOf(BlockPos.containing(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = 0.5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }
        Direction facing = blockState.getValue(FACING);
        Direction.Axis axis = Direction.Axis.Y;
        if (context.state.getBlock() instanceof IRotate def) {
            axis = def.getRotationAxis(context.state);
        }
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        float angle = time * speed % 360;
        float yRadians = axis == Direction.Axis.Z ? Radians.DEG_90 : 0;
        float zRadians = axis.isHorizontal() ? Radians.DEG_90 : 0;
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        float handYRadians = Radians.horizontal(facing);
        float handXRadians = facing == Direction.UP ? Radians.DEG_270 : facing == Direction.DOWN ? Radians.DEG_90 : 0;
        float poleZRadians =
            blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z ? Radians.DEG_90 :
                0;
        SuperByteBuffer hand = CachedBuffers.partial(handPose, blockState).transform(transform).translate(pos);
        SuperByteBuffer shaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState());
        SuperByteBuffer.copyTransform(hand, shaft);
        state.shaft = shaft.center().rotateY(yRadians).rotateZ(zRadians).uncenter().rotateYCentered(angle).light(light)
            .useLevelLight(context.world, worldMatrix4f).extractRenderState();
        if (!context.disabled) {
            Vec3 offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(factor);
            hand.translate(offset);
        }
        hand.rotateYCentered(handYRadians).rotateXCentered(handXRadians);
        SuperByteBuffer pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState);
        SuperByteBuffer.copyTransform(hand, pole);
        state.pole = pole.rotateZCentered(poleZRadians).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        state.hand = hand.light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return state;
    }

    public static class DeployerMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability SuperByteBufferRenderState pole;
        public @UnknownNullability SuperByteBufferRenderState hand;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            shaft.submit(matrices, queue);
            pole.submit(matrices, queue);
            hand.submit(matrices, queue);
        }
    }
}
