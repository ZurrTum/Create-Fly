package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedLargeCogRenderer.EncasedLargeCogRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.shouldOffset;

public class EncasedLargeCogRenderer implements BlockEntityRenderer<SimpleKineticBlockEntity, EncasedLargeCogRenderState> {
    public EncasedLargeCogRenderer(Context context) {
    }

    @Override
    public EncasedLargeCogRenderState createRenderState() {
        return new EncasedLargeCogRenderState();
    }

    @Override
    public void extractRenderState(
        SimpleKineticBlockEntity be,
        EncasedLargeCogRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        @Nullable CrumblingOverlay breakProgress
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        Direction direction = axis.getPositive();
        int color = getTintColor(be);
        float progress = getProgress(be, level);
        boolean offset = shouldOffset(axis, state.blockPos);
        int lightCoords = state.lightCoords;
        state.model = CachedBuffers.partialFacingVertical(
            AllPartialModels.SHAFTLESS_LARGE_COGWHEEL,
            state.blockState,
            direction
        ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        state.axis = axis;
        state.angle = (progress + (offset ? 22.5f : 11.25f)) % 360;
        state.offset = !offset;
        boolean hasTop = state.blockState.getValue(EncasedCogwheelBlock.TOP_SHAFT);
        boolean hasBottom = state.blockState.getValue(EncasedCogwheelBlock.BOTTOM_SHAFT);
        if (hasTop) {
            if (hasBottom) {
                state.shaft = CachedBuffers.partialFacingVertical(
                    AllPartialModels.ENCASED_SHAFT,
                    state.blockState,
                    direction
                ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
            } else {
                state.shaft = CachedBuffers.partialFacing(
                    AllPartialModels.ENCASED_SHAFT_HALF,
                    state.blockState,
                    direction
                ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
            }
        } else if (hasBottom) {
            state.shaft = CachedBuffers.partialFacing(
                AllPartialModels.ENCASED_SHAFT_HALF,
                state.blockState,
                axis.getNegative()
            ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        }
    }

    @Override
    public void submit(
        EncasedLargeCogRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        Pose pose = matrices.last();
        if (state.angle != 0) {
            pose.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
        }
        state.model.submit(matrices, queue);
        if (state.shaft != null) {
            if (state.offset) {
                pose.rotateCentered(state.axis, Radians.DEG_348P75);
            }
            state.shaft.submit(matrices, queue);
        }
    }

    public static class EncasedLargeCogRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability Axis axis;
        public float angle;
        public @Nullable SuperByteBufferRenderState shaft;
        public boolean offset;
    }
}
