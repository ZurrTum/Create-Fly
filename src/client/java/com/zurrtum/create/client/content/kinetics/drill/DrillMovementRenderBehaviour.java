package com.zurrtum.create.client.content.kinetics.drill;

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
import com.zurrtum.create.content.kinetics.drill.DrillBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class DrillMovementRenderBehaviour implements MovementRenderBehaviour {
    @Nullable
    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new DrillActorVisual(visualizationContext, simulationWorld, movementContext);
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
        DrillMovementRenderState state = new DrillMovementRenderState(context.localPos);
        state.layer = RenderType.solid();
        BlockState blockState = context.state;
        state.head = CachedBuffers.partial(AllPartialModels.DRILL_HEAD, blockState);
        Direction facing = blockState.getValue(DrillBlock.FACING);
        float speed = context.contraption.stalled || !VecHelper.isVecPointingTowards(
            context.relativeMotion,
            facing.getOpposite()
        ) ? context.getAnimationSpeed() : 0;
        float time = AnimationTickHolder.getRenderTime() / 20;
        float angle = ((time * speed) % 360);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * AngleHelper.verticalAngle(facing);
        state.zRot = Mth.DEG_TO_RAD * angle;
        state.light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        return state;
    }

    public static class DrillMovementRenderState extends MovementRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer head;
        public float yRot;
        public float xRot;
        public float zRot;
        public int light;
        public Level world;
        public Matrix4f worldMatrix4f;

        public DrillMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            head.center().rotateY(yRot).rotateX(xRot).rotateZ(zRot).uncenter().light(light).useLevelLight(world, worldMatrix4f)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
