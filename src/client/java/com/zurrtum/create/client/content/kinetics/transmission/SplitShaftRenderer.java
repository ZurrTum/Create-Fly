package com.zurrtum.create.client.content.kinetics.transmission;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer.SplitShaftRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class SplitShaftRenderer implements BlockEntityRenderer<SplitShaftBlockEntity, SplitShaftRenderState> {
    public SplitShaftRenderer(Context context) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        int color = getTintColor(be);
        Axis axis = getRotationAxisOf(state.blockState);
        Direction direction1 = axis.getPositive();
        float offset = rotationOffset(state.blockState, axis, state.blockPos);
        float progress = getProgress(be, level);
        state.topAngle = getRotateAngle(progress * be.getRotationSpeedModifier(direction1), offset, direction1);
        state.top = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, state.blockState, direction1)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
        Direction direction2 = axis.getNegative();
        state.bottomAngle = getRotateAngle(progress * be.getRotationSpeedModifier(direction2), offset, direction1);
        state.bottom = CachedBuffers.partialFacing(AllPartialModels.ENCASED_SHAFT_HALF, state.blockState, direction2)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        SplitShaftRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.topAngle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.topAngle, 0.5f, 0.5f, 0.5f);
            state.top.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.top.submit(matrices, queue);
        }
        if (state.bottomAngle != null) {
            matrices.rotateAround(state.bottomAngle, 0.5f, 0.5f, 0.5f);
        }
        state.bottom.submit(matrices, queue);
    }

    public static class SplitShaftRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState top;
        public @UnknownNullability SuperByteBufferRenderState bottom;
        public @Nullable Quaternionf topAngle;
        public @Nullable Quaternionf bottomAngle;
    }
}
