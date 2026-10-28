package com.zurrtum.create.client.content.kinetics.clock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.clock.CuckooClockRenderer.CuckooClockRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlock;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

public class CuckooClockRenderer implements BlockEntityRenderer<CuckooClockBlockEntity, CuckooClockRenderState> {
    public CuckooClockRenderer(Context context) {
    }

    @Override
    public CuckooClockRenderState createRenderState() {
        return new CuckooClockRenderState();
    }

    @Override
    public void extractRenderState(
        CuckooClockBlockEntity be,
        CuckooClockRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(CuckooClockBlock.HORIZONTAL_FACING);
        if (!VisualizationManager.supportsVisualization(level)) {
            state.shaft = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF,
                state.blockState,
                facing.getOpposite()
            ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            Direction.Axis axis = facing.getAxis();
            state.axis = axis;
            state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        }
        state.hourHand = CachedBuffers.partial(AllPartialModels.CUCKOO_HOUR_HAND, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.minuteHand = CachedBuffers.partial(AllPartialModels.CUCKOO_MINUTE_HAND, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        if (behaviour != null) {
            state.hourAngle = behaviour.hourHand.getValue(tickProgress);
            state.minuteAngle = behaviour.minuteHand.getValue(tickProgress);
        }
        state.yRadians = Radians.horizontal(facing.getCounterClockWise());
        state.leftDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_LEFT_DOOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.rightDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_RIGHT_DOOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        float doorAngle = getDoorAngle(be, tickProgress);
        if (doorAngle != 0) {
            state.doorRadians = Mth.DEG_TO_RAD * doorAngle;
            if (be.animationType == Animation.NONE) {
                return;
            }
            state.offset = -(doorAngle / 135) * 0.5f + 0.625f;
            if (state.offset > 0.4f) {
                return;
            }
            PartialModel partialModel =
                be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER;
            state.figure = CachedBuffers.partial(partialModel, state.blockState).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).extractRenderState();
        }
    }

    public static float getDoorAngle(CuckooClockBlockEntity be, float tickProgress) {
        float value = be.animationProgress.getValue(tickProgress);
        if (value < 25 || value >= 70) {
            return 0;
        }
        int step, minValue;
        if (be.animationType != Animation.SURPRISE) {
            minValue = 25;
            step = 5;
        } else if (value >= 29 && value < 62) {
            minValue = 29;
            step = 1;
        } else {
            return 0;
        }
        float local = value - (int) value - step + ((int) value - minValue) % (step * 3);
        if (local < 0) {
            return Mth.lerp((local + 5) / 5.0f, 0, 135);
        }
        if (local < step) {
            return 135;
        }
        return Mth.lerp((local - 5) / 5.0f, 135, 0);
    }

    @Override
    public void submit(
        CuckooClockRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.shaft != null) {
            if (state.angle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
                state.shaft.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.shaft.submit(matrices, queue);
            }
        }
        if (state.yRadians != 0) {
            matrices.rotateYCentered(state.yRadians);
        }
        if (state.hourAngle != 0) {
            matrices.pushPose();
            matrices.rotateXAround(Mth.DEG_TO_RAD * state.hourAngle, 0.125f, 0.375f, 0.5f);
            state.hourHand.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.hourHand.submit(matrices, queue);
        }
        if (state.minuteAngle != 0) {
            matrices.pushPose();
            matrices.rotateXAround(Mth.DEG_TO_RAD * state.minuteAngle, 0.125f, 0.375f, 0.5f);
            state.minuteHand.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.minuteHand.submit(matrices, queue);
        }
        if (state.doorRadians != 0) {
            matrices.pushPose();
            matrices.rotateYAround(-state.doorRadians, 0.125f, 0, 0.375f);
            state.leftDoor.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            matrices.rotateYAround(state.doorRadians, 0.125f, 0, 0.625f);
            state.rightDoor.submit(matrices, queue);
            matrices.popPose();
            if (state.figure != null) {
                matrices.translate(state.offset, 0, 0);
                state.figure.submit(matrices, queue);
            }
        } else {
            state.leftDoor.submit(matrices, queue);
            state.rightDoor.submit(matrices, queue);
        }
    }

    public static class CuckooClockRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState shaft;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
        public @UnknownNullability SuperByteBufferRenderState hourHand;
        public @UnknownNullability SuperByteBufferRenderState minuteHand;
        public float yRadians;
        public float hourAngle;
        public float minuteAngle;
        public @UnknownNullability SuperByteBufferRenderState leftDoor;
        public @UnknownNullability SuperByteBufferRenderState rightDoor;
        public float doorRadians;
        public @Nullable SuperByteBufferRenderState figure;
        public float offset;
    }
}
