package com.zurrtum.create.client.content.kinetics.transmission;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SplitShaftRenderer implements BlockEntityRenderer<SplitShaftBlockEntity, SplitShaftRenderer.SplitShaftRenderState> {
    public SplitShaftRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public SplitShaftRenderState createRenderState() {
        return new SplitShaftRenderState();
    }

    @Override
    public void updateRenderState(
        SplitShaftBlockEntity be,
        SplitShaftRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        state.color = KineticBlockEntityRenderer.getColor(be);
        Axis boxAxis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        BlockPos pos = state.pos;
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
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
    public void render(SplitShaftRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class SplitShaftRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public Color color;
        public Direction direction;
        public SuperByteBuffer top;
        public float topAngle;
        public SuperByteBuffer bottom;
        public float bottomAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            top.light(lightmapCoordinates);
            top.rotateCentered(topAngle, direction);
            top.color(color);
            top.renderInto(matricesEntry, vertexConsumer);
            bottom.light(lightmapCoordinates);
            bottom.rotateCentered(bottomAngle, direction);
            bottom.color(color);
            bottom.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
