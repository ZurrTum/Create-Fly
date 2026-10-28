package com.zurrtum.create.client.content.kinetics.flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelRenderer.FlywheelRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class FlywheelRenderer implements BlockEntityRenderer<FlywheelBlockEntity, FlywheelRenderState> {
    public FlywheelRenderer(Context context) {
    }

    @Override
    public FlywheelRenderState createRenderState() {
        return new FlywheelRenderState();
    }

    @Override
    public void extractRenderState(
        FlywheelBlockEntity be,
        FlywheelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        BlockState blockState = state.blockState;
        int lightCoords = state.lightCoords;
        Axis axis = getRotationAxisOf(blockState);
        int color = getTintColor(be);
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(lightCoords).color(color).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        state.wheel = CachedBuffers.partialFacingVertical(AllPartialModels.FLYWHEEL, blockState, axis.getPositive())
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
        float speed = be.visualSpeed.getValue(tickProgress) * 0.3f;
        state.wheelAngle = be.angle + speed * tickProgress;
    }

    @Override
    public void submit(
        FlywheelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaft.submit(matrices, queue);
        }
        if (state.wheelAngle != 0) {
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.wheelAngle);
        }
        state.wheel.submit(matrices, queue);
    }

    public static class FlywheelRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability Axis axis;
        public float angle;
        public @UnknownNullability SuperByteBufferRenderState wheel;
        public float wheelAngle;
    }
}
