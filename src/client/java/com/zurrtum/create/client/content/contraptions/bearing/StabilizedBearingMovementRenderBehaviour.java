package com.zurrtum.create.client.content.contraptions.bearing;

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
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class StabilizedBearingMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new StabilizedBearingVisual(visualizationContext, simulationWorld, movementContext);
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
        StabilizedBearingMovementRenderState state = new StabilizedBearingMovementRenderState(context.localPos);
        state.layer = RenderLayer.getSolid();
        Direction facing = context.state.get(Properties.FACING);
        state.top = CachedBuffers.partial(AllPartialModels.BEARING_TOP, context.state);
        // rotate to match blockstate
        Quaternionf orientation = BearingVisual.getBlockStateOrientation(facing);
        // rotate against parent
        float angle = getCounterRotationAngle(context, facing, AnimationTickHolder.getPartialTicks()) * facing.getDirection().offset();
        Quaternionf rotation = RotationAxis.of(facing.getUnitVector()).rotationDegrees(angle);
        state.orientation = rotation.mul(orientation);
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        return state;
    }

    static float getCounterRotationAngle(MovementContext context, Direction facing, float renderPartialTicks) {
        if (!context.contraption.canBeStabilized(facing, context.localPos))
            return 0;

        float offset = 0;
        Direction.Axis axis = facing.getAxis();
        AbstractContraptionEntity entity = context.contraption.entity;

        if (entity instanceof ControlledContraptionEntity controlledCE) {
            if (context.contraption.canBeStabilized(facing, context.localPos))
                offset = -controlledCE.getAngle(renderPartialTicks);

        } else if (entity instanceof OrientedContraptionEntity orientedCE) {
            if (axis.isVertical())
                offset = -orientedCE.getViewYRot(renderPartialTicks);
            else {
                if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation().getAxis() == axis)
                    offset = -orientedCE.getViewXRot(renderPartialTicks);
            }
        }
        return offset;
    }

    public static class StabilizedBearingMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer top;
        public Quaternionf orientation;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;

        public StabilizedBearingMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            top.rotateCentered(orientation).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
