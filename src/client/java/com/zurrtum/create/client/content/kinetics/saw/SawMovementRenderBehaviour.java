package com.zurrtum.create.client.content.kinetics.saw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class SawMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public @Nullable ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new SawActorVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        PoseStack.Pose transform,
        Matrix4f worldMatrix4f
    ) {
        SawMovementRenderState state = new SawMovementRenderState();
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(SawBlock.FACING);
        Vec3 facingVec = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        facingVec = context.rotation.apply(facingVec);
        Direction closestToFacing = Direction.getApproximateNearest(facingVec.x, facingVec.y, facingVec.z);
        boolean horizontal = closestToFacing.getAxis().isHorizontal();
        boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
        boolean moving = context.getAnimationSpeed() != 0;
        boolean shouldAnimate = context.contraption.stalled && horizontal || !context.contraption.stalled && !backwards && moving;
        SuperByteBuffer saw;
        float zRadians;
        if (SawBlock.isHorizontal(blockState)) {
            saw = CachedBuffers.partial(
                shouldAnimate ? AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE : AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE,
                blockState
            );
            zRadians = 0;
        } else {
            saw = CachedBuffers.partial(
                shouldAnimate ? AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE : AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE,
                blockState
            );
            zRadians = blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? Radians.DEG_90 : 0;
        }
        BlockPos pos = context.localPos;
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        saw.transform(transform).translate(pos);
        if (!VisualizationManager.supportsVisualization(context.world)) {
            Axis axis = facing.getAxis();
            SuperByteBuffer shaft;
            if (axis.isHorizontal()) {
                shaft = CachedBuffers.partialFacing(
                    AllPartialModels.SHAFT_HALF,
                    blockState.getBlock().rotate(blockState, Rotation.CLOCKWISE_180)
                );
            } else {
                boolean alongFirst = blockState.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
                if (axis == Axis.X) {
                    axis = alongFirst ? Axis.Y : Axis.Z;
                } else if (axis == Axis.Y) {
                    axis = alongFirst ? Axis.X : Axis.Z;
                } else if (axis == Axis.Z) {
                    axis = alongFirst ? Axis.X : Axis.Y;
                }
                shaft = CachedBuffers.block(
                    KineticBlockEntityRenderer.KINETIC_BLOCK,
                    KineticBlockEntityRenderer.shaft(axis)
                );
            }
            float angle = Mth.DEG_TO_RAD * KineticBlockEntityVisual.rotationOffset(blockState, axis, pos);
            Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            SuperByteBuffer.copyTransform(saw, shaft);
            state.shaft = shaft.rotateCentered(angle, direction).light(light)
                .useLevelLight(context.world, worldMatrix4f).extractRenderState();
        }
        float yRadians = Radians.horizontal(facing);
        float xRadians = Radians.vertical(facing);
        state.saw = saw.center().rotateY(yRadians).rotateX(xRadians).rotateZ(zRadians).uncenter().light(light)
            .useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return state;
    }

    public static class SawMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState saw;
        public @Nullable SuperByteBufferRenderState shaft;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            saw.submit(matrices, queue);
            if (shaft != null) {
                shaft.submit(matrices, queue);
            }
        }
    }
}
