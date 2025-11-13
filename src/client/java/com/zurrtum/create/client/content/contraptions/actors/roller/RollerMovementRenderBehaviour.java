package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RollerMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new RollerActorVisual(visualizationContext, simulationWorld, movementContext);
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
        RollerMovementRenderState state = new RollerMovementRenderState(context.localPos);
        state.layer = RenderType.cutoutMipped();
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, blockState);
        float speed = !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            facing.getOpposite()
        ) ? context.getAnimationSpeed() : -context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        state.offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(17 / 16f).add(0, -0.25f, 0);
        float angle = AngleHelper.horizontalAngle(facing);
        state.wheelAngle = AngleHelper.rad(angle);
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        state.rotate = AngleHelper.rad((time * speed) % 360);
        state.light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        state.yRot = Mth.DEG_TO_RAD * 90;
        state.frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState);
        state.frameAngle = AngleHelper.rad(angle + 180);
        return state;
    }

    public static class RollerMovementRenderState extends MovementRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer wheel;
        public Vec3 offset;
        public float wheelAngle;
        public float rotate;
        public int light;
        public Level world;
        public Matrix4f worldMatrix4f;
        public float yRot;
        public SuperByteBuffer frame;
        public float frameAngle;

        public RollerMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            wheel.translate(offset).rotateCentered(wheelAngle, Direction.UP).rotate(rotate, Direction.WEST).translate(0, -.5, .5).rotateY(yRot)
                .light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            frame.rotateCentered(frameAngle, Direction.UP).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
