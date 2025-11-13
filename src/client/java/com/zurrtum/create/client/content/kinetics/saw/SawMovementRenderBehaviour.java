package com.zurrtum.create.client.content.kinetics.saw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

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
        Matrix4f worldMatrix4f
    ) {
        SawMovementRenderState state = new SawMovementRenderState(context.localPos);
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(SawBlock.FACING);
        Vec3 facingVec = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        facingVec = context.rotation.apply(facingVec);
        Direction closestToFacing = Direction.getApproximateNearest(facingVec.x, facingVec.y, facingVec.z);
        boolean horizontal = closestToFacing.getAxis().isHorizontal();
        boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
        boolean moving = context.getAnimationSpeed() != 0;
        boolean shouldAnimate = (context.contraption.stalled && horizontal) || (!context.contraption.stalled && !backwards && moving);
        state.layer = RenderType.cutoutMipped();
        if (SawBlock.isHorizontal(blockState)) {
            state.saw = CachedBuffers.partial(
                shouldAnimate ? AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE : AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE,
                blockState
            );
        } else {
            state.saw = CachedBuffers.partial(
                shouldAnimate ? AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE : AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE,
                blockState
            );
            if (blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE)) {
                state.zRot = Mth.DEG_TO_RAD * 90;
            }
        }
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * AngleHelper.verticalAngle(facing);
        state.light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        if (!VisualizationManager.supportsVisualization(context.world)) {
            Axis axis = facing.getAxis();
            if (axis.isHorizontal()) {
                state.shaft = CachedBuffers.partialFacing(
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
                state.shaft = CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, KineticBlockEntityRenderer.shaft(axis));
            }
            state.angle = Mth.DEG_TO_RAD * KineticBlockEntityVisual.rotationOffset(blockState, axis, context.localPos);
            state.direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        }
        return state;
    }

    public static class SawMovementRenderState extends MovementRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public Level world;
        public SuperByteBuffer saw;
        public Matrix4f worldMatrix4f;
        public float yRot;
        public float xRot;
        public float zRot;
        public int light;
        public SuperByteBuffer shaft;
        public float angle;
        public Direction direction;

        public SawMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            saw.center().rotateY(yRot).rotateX(xRot).rotateZ(zRot).uncenter().light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(light).useLevelLight(world, worldMatrix4f).rotateCentered(angle, direction).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
