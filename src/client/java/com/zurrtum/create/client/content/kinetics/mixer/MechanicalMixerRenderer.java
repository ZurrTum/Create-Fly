package com.zurrtum.create.client.content.kinetics.mixer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.mixer.MechanicalMixerRenderer.MechanicalMixerRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.MechanicalMixerAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class MechanicalMixerRenderer implements BlockEntityRenderer<MechanicalMixerBlockEntity, MechanicalMixerRenderState> {
    public MechanicalMixerRenderer(Context context) {
    }

    @Override
    public MechanicalMixerRenderState createRenderState() {
        return new MechanicalMixerRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalMixerBlockEntity be,
        MechanicalMixerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.cogwheel = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        Axis axis = getRotationAxisOf(state.blockState);
        float speed = be.getSpeed();
        float time = AnimationTickHolder.getRenderTime(level);
        float progress = getProgress(speed, time);
        float offset = rotationOffset(state.blockState, axis, state.blockPos);
        state.axis = axis;
        state.angle = (progress + offset) % 360;
        state.headOffset = getRenderedHeadOffset(be, tickProgress);
        state.pole = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.head = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        MechanicalMixerAnimationBehaviour behaviour = (MechanicalMixerAnimationBehaviour) be.getBehaviour(
            AnimationBehaviour.TYPE);
        if (behaviour == null) {
            state.headAngle = progress % 360;
        } else {
            state.headAngle = (progress + behaviour.getOffset(speed, tickProgress)) % 360;
        }
    }

    public static float getRenderedHeadOffset(MechanicalMixerBlockEntity be, float partialTicks) {
        if (be.running) {
            if (be.runningTicks == 20) {
                return -1.4375f;
            }
            float runningTicks = be.runningTicks + partialTicks;
            if (runningTicks < 20) {
                return Mth.cos(runningTicks * 0.05f * Math.PI) / 2 - 0.9375f;
            }
            return Mth.cos((41 - runningTicks) * 0.05f * Math.PI) / 2 - 0.9375f;
        }
        return -0.4375f;
    }

    @Override
    public void submit(
        MechanicalMixerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.cogwheel.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.cogwheel.submit(matrices, queue);
        }
        Pose pose = matrices.last();
        pose.translate(0, state.headOffset, 0);
        state.pole.submit(matrices, queue);
        if (state.headAngle != 0) {
            pose.rotateYCentered(Mth.DEG_TO_RAD * state.headAngle);
        }
        state.head.submit(matrices, queue);
    }

    public static class MechanicalMixerRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState cogwheel;
        public @UnknownNullability SuperByteBufferRenderState pole;
        public @UnknownNullability SuperByteBufferRenderState head;
        public @UnknownNullability Axis axis;
        public float angle;
        public float headOffset;
        public float headAngle;
    }
}
