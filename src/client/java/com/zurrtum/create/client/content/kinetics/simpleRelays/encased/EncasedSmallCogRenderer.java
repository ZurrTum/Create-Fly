package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedSmallCogRenderer.EncasedSmallCogRenderState;
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
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class EncasedSmallCogRenderer implements BlockEntityRenderer<SimpleKineticBlockEntity, EncasedSmallCogRenderState> {
    public EncasedSmallCogRenderer(Context context) {
    }

    @Override
    public EncasedSmallCogRenderState createRenderState() {
        return new EncasedSmallCogRenderState();
    }

    @Override
    public void extractRenderState(
        SimpleKineticBlockEntity be,
        EncasedSmallCogRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        @Nullable CrumblingOverlay breakProgress
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        Direction direction = axis.getPositive();
        int color = getTintColor(be);
        state.angle = getRotateAngleWithoutBeOffset(axis, direction, be, state, level);
        boolean hasTop = state.blockState.getValue(EncasedCogwheelBlock.TOP_SHAFT);
        boolean hasBottom = state.blockState.getValue(EncasedCogwheelBlock.BOTTOM_SHAFT);
        int lightCoords = state.lightCoords;
        state.model = CachedBuffers.partialFacingVertical(
            AllPartialModels.SHAFTLESS_COGWHEEL_CULL,
            state.blockState,
            direction
        ).cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
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
        EncasedSmallCogRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
        }
        state.model.submit(matrices, queue);
        if (state.shaft != null) {
            state.shaft.submit(matrices, queue);
        }
    }

    public static class EncasedSmallCogRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
        public @Nullable SuperByteBufferRenderState shaft;
    }
}
