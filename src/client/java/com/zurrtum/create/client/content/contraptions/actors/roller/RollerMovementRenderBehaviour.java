package com.zurrtum.create.client.content.contraptions.actors.roller;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
        Vec3d camera,
        TextRenderer textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }
        RollerMovementRenderState state = new RollerMovementRenderState(context.localPos);
        state.layer = RenderLayer.getCutoutMipped();
        BlockState blockState = context.state;
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        state.wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, blockState);
        float speed = !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            facing.getOpposite()
        ) ? context.getAnimationSpeed() : -context.getAnimationSpeed();
        if (context.contraption.stalled) {
            speed = 0;
        }
        state.offset = Vec3d.of(facing.getVector()).multiply(17 / 16f).add(0, -0.25f, 0);
        float angle = AngleHelper.horizontalAngle(facing);
        state.wheelAngle = AngleHelper.rad(angle);
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        state.rotate = AngleHelper.rad((time * speed) % 360);
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        state.yRot = MathHelper.RADIANS_PER_DEGREE * 90;
        state.frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, blockState);
        state.frameAngle = AngleHelper.rad(angle + 180);
        return state;
    }

    public static class RollerMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer wheel;
        public Vec3d offset;
        public float wheelAngle;
        public float rotate;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;
        public float yRot;
        public SuperByteBuffer frame;
        public float frameAngle;

        public RollerMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            wheel.translate(offset).rotateCentered(wheelAngle, Direction.UP).rotate(rotate, Direction.WEST).translate(0, -.5, .5).rotateY(yRot)
                .light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            frame.rotateCentered(frameAngle, Direction.UP).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
