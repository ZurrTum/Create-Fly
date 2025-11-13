package com.zurrtum.create.client.content.kinetics.transmission;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SplitShaftRenderer implements BlockEntityRenderer<SplitShaftBlockEntity, SplitShaftRenderer.SplitShaftRenderState> {
    public SplitShaftRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SplitShaftRenderState createRenderState() {
        return new SplitShaftRenderState();
    }

    @Override
    public void extractRenderState(
        SplitShaftBlockEntity be,
        SplitShaftRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.solid();
        state.color = KineticBlockEntityRenderer.getColor(be);
        Axis boxAxis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        BlockPos pos = state.blockPos;
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, boxAxis);
        float angle = (time * be.getSpeed() * 3f / 10) % 360;
        state.direction = switch (boxAxis) {
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
            case X -> Direction.EAST;
        };
        Direction bottom = switch (boxAxis) {
            case Y -> Direction.DOWN;
            case Z -> Direction.NORTH;
            case X -> Direction.WEST;
        };
        state.top = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, state.direction);
        state.topAngle = getAngle(be, angle, offset, state.direction);
        state.bottom = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, bottom);
        state.bottomAngle = getAngle(be, angle, offset, bottom);
    }

    private static float getAngle(SplitShaftBlockEntity be, float angle, float offset, Direction direction) {
        angle *= be.getRotationSpeedModifier(direction);
        angle += offset;
        return angle / 180f * (float) Math.PI;
    }

    @Override
    public void submit(SplitShaftRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class SplitShaftRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public Color color;
        public Direction direction;
        public SuperByteBuffer top;
        public float topAngle;
        public SuperByteBuffer bottom;
        public float bottomAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            top.light(lightCoords);
            top.rotateCentered(topAngle, direction);
            top.color(color);
            top.renderInto(matricesEntry, vertexConsumer);
            bottom.light(lightCoords);
            bottom.rotateCentered(bottomAngle, direction);
            bottom.color(color);
            bottom.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
