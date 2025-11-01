package com.zurrtum.create.client.content.contraptions.actors.harvester;

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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import static com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer.PIVOT;

public class HarvesterMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new HarvesterActorVisual(visualizationContext, simulationWorld, movementContext);
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
        HarvesterMovementRenderState state = new HarvesterMovementRenderState(context.localPos);
        BlockState blockState = context.state;
        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        state.layer = RenderLayer.getCutoutMipped();
        state.model = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState);
        float speed = !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()) ? context.getAnimationSpeed() : 0;
        if (context.contraption.stalled) {
            speed = 0;
        }
        float originOffset = 1 / 16f;
        state.rotOffset = new Vec3d(0, PIVOT.y * originOffset, PIVOT.z * originOffset);
        float time = AnimationTickHolder.getRenderTime(context.world) / 20;
        state.angle = AngleHelper.rad((time * speed) % 360);
        state.horizontalAngle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        return state;
    }

    public static class HarvesterMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public Vec3d rotOffset;
        public float angle;
        public float horizontalAngle;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;

        public HarvesterMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.rotateCentered(horizontalAngle, Direction.UP).translate(rotOffset.x, rotOffset.y, rotOffset.z).rotate(angle, Direction.WEST)
                .translate(-rotOffset.x, -rotOffset.y, -rotOffset.z).light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
