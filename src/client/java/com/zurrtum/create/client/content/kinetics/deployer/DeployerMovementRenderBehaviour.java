package com.zurrtum.create.client.content.kinetics.deployer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

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
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        DeployerMovementRenderState state = new DeployerMovementRenderState(context.localPos);
        state.layer = RenderTypes.solidMovingBlock();
        BlockState blockState = context.state;
        Mode mode = context.blockEntityData.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);
        float speed = context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        state.shaft = CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState());
        state.pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState);
        state.hand = CachedBuffers.partial(handPose, blockState);
        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3 center = VecHelper.getCenterOf(BlockPos.containing(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = .5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }
        Direction facing = blockState.getValue(FACING);
        Direction.Axis axis = Direction.Axis.Y;
        if (context.state.getBlock() instanceof IRotate def) {
            axis = def.getRotationAxis(context.state);
        }
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        state.angle = (time * speed) % 360;
        state.yRot = axis == Direction.Axis.Z ? Mth.DEG_TO_RAD * 90 : 0;
        state.zRot = axis.isHorizontal() ? Mth.DEG_TO_RAD * 90 : 0;
        state.light = LevelRenderer.getLightCoords(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        if (!context.disabled) {
            state.offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(factor);
        }
        state.upAngle = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.eastAngle = Mth.DEG_TO_RAD * (facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0);
        state.southAngle = Mth.DEG_TO_RAD * ((blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90 : 0);
        return state;
    }

    public static class DeployerMovementRenderState extends MovementRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer shaft;
        public SuperByteBuffer pole;
        public SuperByteBuffer hand;
        public float yRot;
        public float zRot;
        public float angle;
        public int light;
        public Level world;
        public Matrix4f worldMatrix4f;
        public Vec3 offset;
        public float upAngle;
        public float eastAngle;
        public float southAngle;

        public DeployerMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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
