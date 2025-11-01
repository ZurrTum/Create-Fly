package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new DeployerActorVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    public MovementRenderState getRenderState(
        Vec3d camera,
        TextRenderer textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        DeployerMovementRenderState state = new DeployerMovementRenderState(context.localPos);
        state.layer = RenderLayer.getSolid();
        BlockState blockState = context.state;
        Mode mode = context.blockEntityData.get("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);
        float speed = context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        state.shaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState());
        state.pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState);
        state.hand = CachedBuffers.partial(handPose, blockState);
        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3d center = VecHelper.getCenterOf(BlockPos.ofFloored(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }
        Direction facing = blockState.get(FACING);
        Direction.Axis axis = Direction.Axis.Y;
        if (context.state.getBlock() instanceof IRotate def) {
            axis = def.getRotationAxis(context.state);
        }
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        state.angle = (time * speed) % 360;
        state.yRot = axis == Direction.Axis.Z ? MathHelper.RADIANS_PER_DEGREE * 90 : 0;
        state.zRot = axis.isHorizontal() ? MathHelper.RADIANS_PER_DEGREE * 90 : 0;
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        if (!context.disabled) {
            state.offset = Vec3d.of(facing.getVector()).multiply(factor);
        }
        state.upAngle = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.eastAngle = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0);
        state.southAngle = MathHelper.RADIANS_PER_DEGREE * ((blockState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90 : 0);
        return state;
    }

    public static class DeployerMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer shaft;
        public SuperByteBuffer pole;
        public SuperByteBuffer hand;
        public float yRot;
        public float zRot;
        public float angle;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;
        public Vec3d offset;
        public float upAngle;
        public float eastAngle;
        public float southAngle;

        public DeployerMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            shaft.center().rotateY(yRot).rotateZ(zRot).uncenter().rotateCentered(angle, Direction.UP).light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
            if (offset != null) {
                pole.translate(offset);
                hand.translate(offset);
            }
            pole.rotateCentered(upAngle, Direction.UP).rotateCentered(eastAngle, Direction.EAST).rotateCentered(southAngle, Direction.SOUTH)
                .light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            hand.rotateCentered(upAngle, Direction.UP).rotateCentered(eastAngle, Direction.EAST).light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
