package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
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

public class PortableStorageInterfaceMovementRender implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new PSIActorVisual(visualizationContext, simulationWorld, movementContext);
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
        PortableStorageInterfaceMovementRenderState state = new PortableStorageInterfaceMovementRenderState(context.localPos);
        state.layer = RenderLayer.getSolid();
        BlockState blockState = context.state;
        float renderPartialTicks = AnimationTickHolder.getPartialTicks();
        LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
        state.middle = CachedBuffers.partial(PortableStorageInterfaceRenderer.getMiddleForState(blockState, animation.settled()), blockState);
        state.top = CachedBuffers.partial(PortableStorageInterfaceRenderer.getTopForState(blockState), blockState);
        Direction facing = blockState.get(PortableStorageInterfaceBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        state.topOffset = animation.getValue(renderPartialTicks);
        state.middleOffset = state.topOffset * 0.5f + 0.375f;
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        return state;
    }

    public static class PortableStorageInterfaceMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer middle;
        public SuperByteBuffer top;
        public float yRot;
        public float xRot;
        public float middleOffset;
        public float topOffset;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;

        public PortableStorageInterfaceMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            middle.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, middleOffset, 0).light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
            top.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, topOffset, 0).light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
