package com.zurrtum.create.client.content.logistics.packagePort.frogport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.packagePort.frogport.FrogportRenderer.FrogportRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.NameplateRenderState;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getXRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getYRotateAngle;

public class FrogportRenderer implements BlockEntityRenderer<FrogportBlockEntity, FrogportRenderState> {
    public FrogportRenderer(Context context) {
    }

    @Override
    public FrogportRenderState createRenderState() {
        return new FrogportRenderState();
    }

    @Override
    public void extractRenderState(
        FrogportBlockEntity be,
        FrogportRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        String filter = be.addressFilter;
        if (VisualizationManager.supportsVisualization(level)) {
            if (filter == null || filter.isBlank()) {
                return;
            }
            BlockPos blockPos = be.getBlockPos();
            state.name = SmartBlockEntityRenderer.getNameplateRenderState(
                be,
                blockPos,
                cameraPos,
                Component.literal(filter),
                1,
                SmartBlockEntityRenderer.getLightCoords(level, blockPos)
            );
            if (state.name != null) {
                state.blockPos = blockPos;
                state.blockEntityType = be.getType();
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        if (filter != null && !filter.isBlank()) {
            state.name = SmartBlockEntityRenderer.getNameplateRenderState(
                be,
                state.blockPos,
                cameraPos,
                Component.literal(filter),
                1,
                state.lightCoords
            );
        }
        FrogportRenderData data = state.data = new FrogportRenderData();
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        data.body = CachedBuffers.partial(AllPartialModels.FROGPORT_BODY, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Vec3 diff;
        float tongueLength, headPitch, headPitchModifier;
        boolean animating = be.isAnimationInProgress();
        boolean depositing = be.currentlyDepositing;
        if (be.target != null) {
            diff = be.target.getExactTargetLocation(be, level, state.blockPos)
                .subtract(0, animating && depositing ? 0 : 0.75, 0).subtract(Vec3.atCenterOf(state.blockPos));
            float tonguePitch = (float) Mth.atan2(diff.y, diff.multiply(1, 0, 1).length() + 3 / 16.0f) * Mth.RAD_TO_DEG;
            tongueLength = Math.max((float) diff.length(), 1);
            headPitch = Mth.clamp(tonguePitch * 2, 60, 100);
            data.tonguePitch = Axis.XP.rotation(Mth.DEG_TO_RAD * tonguePitch);
        } else {
            diff = Vec3.ZERO;
            tongueLength = 0;
            headPitch = 80;
        }
        if (animating) {
            float progress = be.animationProgress.getValue(tickProgress);
            float scale, itemDistance;
            if (depositing) {
                double modifier = Math.max(0, 1 - Math.pow((progress - 0.25) * 4 - 1, 4));
                itemDistance = (float) Math.max(
                    tongueLength * Math.min(1, (progress - 0.25) * 3),
                    tongueLength * modifier
                );
                tongueLength *= (float) Math.max(0, 1 - Math.pow((progress * 1.25 - 0.25) * 4 - 1, 4));
                headPitchModifier = (float) Math.max(0, 1 - Math.pow(progress * 1.25 * 2 - 1, 4));
                scale = 0.25f + progress * 3 / 4;

            } else {
                tongueLength *= (float) Math.pow(Math.max(0, 1 - progress * 1.25), 5);
                headPitchModifier = 1 - (float) Math.min(1, Math.max(0, (Math.pow(progress * 1.5, 2) - 0.5) * 2));
                scale = (float) Math.max(0.5, 1 - progress * 1.25);
                itemDistance = tongueLength;
            }
            if (be.animatedPackage != null && scale >= 0.45) {
                Identifier key = BuiltInRegistries.ITEM.getKey(be.animatedPackage.getItem());
                if (key != BuiltInRegistries.ITEM.getDefaultKey()) {
                    data.box = CachedBuffers.partial(AllPartialModels.PACKAGES.get(key), state.blockState)
                        .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
                    data.boxOffset = diff.normalize().scale(itemDistance).add(0, depositing ? -0.5625f : 0.1875f, 0);
                    data.boxScale = scale;
                    if (depositing) {
                        data.rig = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(key), state.blockState)
                            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
                    }
                }
            }
        } else {
            tongueLength = 0;
            float anticipation = be.anticipationProgress.getValue(tickProgress);
            headPitchModifier = anticipation > 0 ? (float) Math.max(0, 1 - Math.pow(anticipation * 2.5 - 1, 4)) : 0;
        }
        headPitch *= headPitchModifier;
        float openProgress = be.manualOpenAnimationProgress.getValue(tickProgress);
        headPitch = Math.max(headPitch, openProgress * 60);
        data.headPitch = getXRotateAngle(headPitch);
        tongueLength = Math.max(tongueLength, openProgress * 0.25f);
        data.yRot = getYRotateAngle(be.getYaw());
        data.head = CachedBuffers.partial(
            be.goggles ? AllPartialModels.FROGPORT_HEAD_GOGGLES : AllPartialModels.FROGPORT_HEAD,
            state.blockState
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.tongue = CachedBuffers.partial(AllPartialModels.FROGPORT_TONGUE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.tongueScale = tongueLength / 0.4375f;
    }

    @Override
    public void submit(
        FrogportRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.name != null) {
            state.name.submit(matrices, queue, cameraState);
        }
        if (state.data != null) {
            state.data.submit(matrices, queue);
        }
    }

    public static class FrogportRenderState extends BlockEntityRenderState {
        public @Nullable NameplateRenderState name;
        public @Nullable FrogportRenderData data;
    }

    public static class FrogportRenderData {
        public @UnknownNullability SuperByteBufferRenderState body;
        public @Nullable Quaternionf tonguePitch;
        public @Nullable Quaternionf yRot;
        public @UnknownNullability SuperByteBufferRenderState head;
        public @Nullable Quaternionf headPitch;
        public @UnknownNullability SuperByteBufferRenderState tongue;
        public float tongueScale;
        public @Nullable SuperByteBufferRenderState rig;
        public @Nullable SuperByteBufferRenderState box;
        public @UnknownNullability Vec3 boxOffset;
        public float boxScale;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            if (box != null) {
                matrices.pushPose();
                matrices.translate(boxOffset);
                SuperByteBuffer.scaleAround(matrices.last(), boxScale, 0.5f, 0.5f, 0.5f);
                box.submit(matrices, queue);
                if (rig != null) {
                    rig.submit(matrices, queue);
                }
                matrices.popPose();
            }
            if (yRot != null) {
                matrices.rotateAround(yRot, 0.5f, 0.5f, 0.5f);
            }
            body.submit(matrices, queue);
            matrices.pushPose();
            matrices.translate(0.5f, 0.625f, 0.6875f);
            if (tonguePitch != null) {
                matrices.mulPose(tonguePitch);
            }
            matrices.scale(1, 1, tongueScale);
            matrices.translate(-0.5f, -0.625f, -0.6875f);
            tongue.submit(matrices, queue);
            matrices.popPose();
            if (headPitch != null) {
                matrices.rotateAround(headPitch, 0.5f, 0.625f, 0.6875f);
            }
            head.submit(matrices, queue);
        }
    }
}
