package com.zurrtum.create.client.content.kinetics.mixer;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
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
        state.angle = getRotateAngle(progress, offset, axis);
        state.headOffset = getRenderedHeadOffset(be, tickProgress);
        state.pole = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.head = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        MechanicalMixerAnimationBehaviour behaviour = (MechanicalMixerAnimationBehaviour) be.getBehaviour(
            AnimationBehaviour.TYPE);
        state.headAngle = getRotateAngle(
            progress,
            getHeadRotationOffset(behaviour, speed, tickProgress),
            Direction.UP
        );
    }

    static float getHeadRotationOffset(
        @Nullable MechanicalMixerAnimationBehaviour behaviour,
        float speed,
        float tickProgress
    ) {
        return behaviour == null ? 0 : behaviour.getOffset(speed, tickProgress);
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
        if (state.angle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
            state.cogwheel.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.cogwheel.submit(matrices, queue);
        }
        matrices.translate(0, state.headOffset, 0);
        state.pole.submit(matrices, queue);
        if (state.headAngle != null) {
            matrices.rotateAround(state.headAngle, 0.5f, 0.5f, 0.5f);
        }
        state.head.submit(matrices, queue);
    }

    public static class MechanicalMixerRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState cogwheel;
        public @UnknownNullability SuperByteBufferRenderState pole;
        public @UnknownNullability SuperByteBufferRenderState head;
        public @Nullable Quaternionf angle;
        public float headOffset;
        public @Nullable Quaternionf headAngle;
    }
}
