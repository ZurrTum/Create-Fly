package com.zurrtum.create.client.content.kinetics.saw;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
        Vec3d camera,
        TextRenderer textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        SawMovementRenderState state = new SawMovementRenderState(context.localPos);
        BlockState blockState = context.state;
        Direction facing = blockState.get(SawBlock.FACING);
        Vec3d facingVec = Vec3d.of(facing.getVector());
        facingVec = context.rotation.apply(facingVec);
        Direction closestToFacing = Direction.getFacing(facingVec.x, facingVec.y, facingVec.z);
        boolean horizontal = closestToFacing.getAxis().isHorizontal();
        boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
        boolean moving = context.getAnimationSpeed() != 0;
        boolean shouldAnimate = (context.contraption.stalled && horizontal) || (!context.contraption.stalled && !backwards && moving);
        state.layer = RenderLayer.getCutoutMipped();
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
            if (blockState.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE)) {
                state.zRot = MathHelper.RADIANS_PER_DEGREE * 90;
            }
        }
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.verticalAngle(facing);
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        if (!VisualizationManager.supportsVisualization(context.world)) {
            Axis axis = facing.getAxis();
            if (axis.isHorizontal()) {
                state.shaft = CachedBuffers.partialFacing(
                    AllPartialModels.SHAFT_HALF,
                    blockState.getBlock().rotate(blockState, BlockRotation.CLOCKWISE_180)
                );
            } else {
                boolean alongFirst = blockState.get(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
                if (axis == Axis.X) {
                    axis = alongFirst ? Axis.Y : Axis.Z;
                } else if (axis == Axis.Y) {
                    axis = alongFirst ? Axis.X : Axis.Z;
                } else if (axis == Axis.Z) {
                    axis = alongFirst ? Axis.X : Axis.Y;
                }
                state.shaft = CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, KineticBlockEntityRenderer.shaft(axis));
            }
            state.angle = MathHelper.RADIANS_PER_DEGREE * KineticBlockEntityVisual.rotationOffset(blockState, axis, context.localPos);
            state.direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
        }
        return state;
    }

    public static class SawMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public World world;
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
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            saw.center().rotateY(yRot).rotateX(xRot).rotateZ(zRot).uncenter().light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(light).useLevelLight(world, worldMatrix4f).rotateCentered(angle, direction).renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
