package com.zurrtum.create.client.content.kinetics.gearbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GearboxRenderer implements BlockEntityRenderer<GearboxBlockEntity, GearboxRenderer.GearboxRenderState> {
    public GearboxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public GearboxRenderState createRenderState() {
        return new GearboxRenderState();
    }

    @Override
    public void extractRenderState(
        GearboxBlockEntity be,
        GearboxRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.solid();
        Axis boxAxis = state.blockState.getValue(BlockStateProperties.AXIS);
        BlockPos pos = state.blockPos;
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float speed = be.getSpeed();
        BlockPos source = null;
        Direction sourceFacing = null;
        if (speed != 0 && be.source != null) {
            source = be.source.subtract(state.blockPos);
            sourceFacing = Direction.getApproximateNearest(source.getX(), source.getY(), source.getZ());
        }
        state.color = KineticBlockEntityRenderer.getColor(be);
        float angle = (time * speed * 3f / 10) % 360;
        if (boxAxis != Axis.Y) {
            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, Axis.Y);
            state.down = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.DOWN);
            state.downAngle = getAngle(angle, offset, Direction.DOWN, source, sourceFacing);
            state.up = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.UP);
            state.upAngle = getAngle(angle, offset, Direction.UP, source, sourceFacing);
        }
        if (boxAxis != Axis.Z) {
            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, Axis.Z);
            state.north = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.NORTH);
            state.northAngle = getAngle(angle, offset, Direction.NORTH, source, sourceFacing);
            state.south = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.SOUTH);
            state.southAngle = getAngle(angle, offset, Direction.SOUTH, source, sourceFacing);
        }
        if (boxAxis != Axis.X) {
            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, Axis.X);
            state.west = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.WEST);
            state.westAngle = getAngle(angle, offset, Direction.WEST, source, sourceFacing);
            state.east = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, Direction.EAST);
            state.eastAngle = getAngle(angle, offset, Direction.EAST, source, sourceFacing);
        }
    }

    private static float getAngle(float angle, float offset, Direction direction, BlockPos source, Direction sourceFacing) {
        if (source != null) {
            if (sourceFacing.getAxis() == direction.getAxis())
                angle *= sourceFacing == direction ? 1 : -1;
            else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
                angle *= -1;
        }
        angle += offset;
        return angle / 180f * (float) Math.PI;
    }

    @Override
    public void submit(GearboxRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class GearboxRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public Color color;
        public SuperByteBuffer down;
        public float downAngle;
        public SuperByteBuffer up;
        public float upAngle;
        public SuperByteBuffer north;
        public float northAngle;
        public SuperByteBuffer south;
        public float southAngle;
        public SuperByteBuffer west;
        public float westAngle;
        public SuperByteBuffer east;
        public float eastAngle;

        private void render(SuperByteBuffer model, float angle, Direction axis, PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.light(lightCoords);
            model.rotateCentered(angle, axis);
            model.color(color);
            model.renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (down != null) {
                render(down, downAngle, Direction.UP, matricesEntry, vertexConsumer);
                render(up, upAngle, Direction.UP, matricesEntry, vertexConsumer);
            }
            if (north != null) {
                render(north, northAngle, Direction.SOUTH, matricesEntry, vertexConsumer);
                render(south, southAngle, Direction.SOUTH, matricesEntry, vertexConsumer);
            }
            if (west != null) {
                render(west, westAngle, Direction.EAST, matricesEntry, vertexConsumer);
                render(east, eastAngle, Direction.EAST, matricesEntry, vertexConsumer);
            }
        }
    }
}
