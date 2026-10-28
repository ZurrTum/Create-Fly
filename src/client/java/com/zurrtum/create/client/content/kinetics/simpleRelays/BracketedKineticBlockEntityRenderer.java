package com.zurrtum.create.client.content.kinetics.simpleRelays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer.BracketedKineticRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.shouldOffset;

public class BracketedKineticBlockEntityRenderer implements BlockEntityRenderer<BracketedKineticBlockEntity, BracketedKineticRenderState> {
    public BracketedKineticBlockEntityRenderer(Context context) {
    }

    @Override
    public BracketedKineticRenderState createRenderState() {
        return new BracketedKineticRenderState();
    }

    @Override
    public void extractRenderState(
        BracketedKineticBlockEntity be,
        BracketedKineticRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        BlockState blockState = state.blockState;
        Axis axis = getRotationAxisOf(blockState);
        state.axis = axis;
        Direction direction = axis.getPositive();
        int color = getTintColor(be);
        float progress = getProgress(be, level);
        float offset;
        SuperByteBuffer model;
        if (blockState.is(AllBlocks.LARGE_COGWHEEL)) {
            float shaftOffset;
            if (shouldOffset(axis, state.blockPos)) {
                offset = shaftOffset = 22.5f;
            } else {
                offset = 11.25f;
                shaftOffset = 0;
            }
            model = CachedBuffers.partialFacingVertical(
                AllPartialModels.SHAFTLESS_LARGE_COGWHEEL,
                blockState,
                direction
            );
            state.shaftAngle = (progress + shaftOffset) % 360;
            state.shaft = CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, blockState, direction)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
        } else {
            offset = rotationOffset(blockState, axis, state.blockPos);
            model = CachedBuffers.block(KINETIC_BLOCK, blockState);
        }
        state.angle = (progress + offset) % 360;
        state.model = model.cardinalLighting(cardinalLighting).light(state.lightCoords).color(color)
            .extractRenderState();
    }

    @Override
    public void submit(
        BracketedKineticRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.shaft != null) {
            if (state.shaftAngle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.shaftAngle);
                state.shaft.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.shaft.submit(matrices, queue);
            }
        }
        if (state.angle != 0) {
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
        }
        state.model.submit(matrices, queue);
    }

    public static float getShaftAngleOffset(Axis axis, BlockPos pos) {
        if (shouldOffset(axis, pos)) {
            return 22.5f;
        }
        return 0;
    }

    public static class BracketedKineticRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable SuperByteBufferRenderState shaft;
        public @UnknownNullability Axis axis;
        public float angle;
        public float shaftAngle;
    }
}
