package com.zurrtum.create.client.content.kinetics.crank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankRenderer.HandCrankRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

public class HandCrankRenderer implements BlockEntityRenderer<HandCrankBlockEntity, HandCrankRenderState> {
    public HandCrankRenderer(Context context) {
    }

    @Override
    public HandCrankRenderState createRenderState() {
        return new HandCrankRenderState();
    }

    @Override
    public void extractRenderState(
        HandCrankBlockEntity be,
        HandCrankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        int color = getTintColor(be);
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        state.model = CachedBuffers.partialFacingVertical(AllPartialModels.HAND_CRANK_BASE, state.blockState, facing)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
        state.handleAngle = getHandCrankIndependentAngle(be, tickProgress);
        state.handle = CachedBuffers.partialFacing(
            AllPartialModels.HAND_CRANK_HANDLE,
            state.blockState,
            facing.getOpposite()
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        HandCrankRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.model.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model.submit(matrices, queue);
        }
        if (state.handleAngle != 0) {
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.handleAngle);
        }
        state.handle.submit(matrices, queue);
    }

    public static float getHandCrankIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return be.independentAngle + partialTicks * be.chasingAngularVelocity;
    }

    public static class HandCrankRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability SuperByteBufferRenderState handle;
        public @UnknownNullability Axis axis;
        public float angle;
        public float handleAngle;
    }
}
