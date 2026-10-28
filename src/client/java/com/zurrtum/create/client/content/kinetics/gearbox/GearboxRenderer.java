package com.zurrtum.create.client.content.kinetics.gearbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.gearbox.GearboxRenderer.GearboxRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getProgress;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

public class GearboxRenderer implements BlockEntityRenderer<GearboxBlockEntity, GearboxRenderState> {
    public GearboxRenderer(Context context) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        float speed = be.getSpeed();
        Axis axis = state.blockState.getValue(BlockStateProperties.AXIS);
        boolean flag;
        if (speed != 0 && be.source != null) {
            BlockPos sourcePos = be.source.subtract(state.blockPos);
            Direction source = Direction.getApproximateNearest(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
            Axis sourceAxis = source.getAxis();
            boolean positive = source.getAxisDirection() == AxisDirection.POSITIVE;
            if (axis == Axis.Y) {
                flag = (sourceAxis == Axis.Z) == positive;
            } else {
                flag = (sourceAxis == Axis.Y) == positive;
            }
        } else {
            flag = true;
        }
        int color = getTintColor(be);
        float angle = getProgress(speed, level) % 360;
        switch (axis) {
            case Y -> updateState(state, angle, cardinalLighting, color, flag, Axis.Z, Axis.X);
            case Z -> updateState(state, angle, cardinalLighting, color, flag, Axis.Y, Axis.X);
            case X -> updateState(state, angle, cardinalLighting, color, flag, Axis.Y, Axis.Z);
        }
    }

    private static void updateState(
        GearboxRenderState state,
        float angle,
        @Nullable CardinalLighting cardinalLighting,
        int color,
        boolean flag,
        Axis axis1,
        Axis axis2
    ) {
        BlockState blockState = state.blockState;
        float offset1 = KineticBlockEntityVisual.rotationOffset(blockState, axis1, state.blockPos);
        float offset2 = KineticBlockEntityVisual.rotationOffset(blockState, axis2, state.blockPos);
        Direction direction1 = axis1.getPositive();
        Direction direction2 = axis2.getPositive();
        state.axis1 = axis1;
        state.axis2 = axis2;
        if (flag) {
            state.angle0 = angle + offset1;
            state.angle1 = offset1 - angle;
            state.angle2 = offset2 - angle;
            state.angle3 = angle + offset2;
        } else {
            state.angle0 = offset1 - angle;
            state.angle1 = angle + offset1;
            state.angle2 = angle + offset2;
            state.angle3 = offset2 - angle;
        }
        int lightCoords = state.lightCoords;
        state.model0 = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, blockState, direction1)
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        state.model1 = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, blockState, axis1.getNegative())
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        state.model2 = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, blockState, direction2)
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        state.model3 = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, blockState, axis2.getNegative())
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        GearboxRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle0 != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis1, Mth.DEG_TO_RAD * state.angle0);
            state.model0.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model0.submit(matrices, queue);
        }
        if (state.angle1 != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis1, Mth.DEG_TO_RAD * state.angle1);
            state.model1.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model1.submit(matrices, queue);
        }
        if (state.angle2 != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis2, Mth.DEG_TO_RAD * state.angle2);
            state.model2.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model2.submit(matrices, queue);
        }
        if (state.angle3 != 0) {
            matrices.rotateCentered(state.axis2, Mth.DEG_TO_RAD * state.angle3);
        }
        state.model3.submit(matrices, queue);
    }

    public static class GearboxRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model0;
        public @UnknownNullability SuperByteBufferRenderState model1;
        public @UnknownNullability SuperByteBufferRenderState model2;
        public @UnknownNullability SuperByteBufferRenderState model3;
        public @UnknownNullability Axis axis1;
        public @UnknownNullability Axis axis2;
        public float angle0;
        public float angle1;
        public float angle2;
        public float angle3;
    }
}
