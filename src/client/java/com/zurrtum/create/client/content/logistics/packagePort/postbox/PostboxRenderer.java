package com.zurrtum.create.client.content.logistics.packagePort.postbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.logistics.packagePort.postbox.PostboxRenderer.PostboxRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.NameplateRenderState;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class PostboxRenderer implements BlockEntityRenderer<PostboxBlockEntity, PostboxRenderState> {
    public PostboxRenderer(Context context) {
    }

    @Override
    public PostboxRenderState createRenderState() {
        return new PostboxRenderState();
    }

    @Override
    public void extractRenderState(
        PostboxBlockEntity be,
        PostboxRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.flag = CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, state.blockState).cardinalLighting(level)
            .light(state.lightCoords).extractRenderState();
        state.yRot = 180 - state.blockState.getValue(PostboxBlock.FACING).toYRot();
        LerpedFloat flag = be.flag;
        float value = flag.getValue(tickProgress);
        float progress = (float) Math.pow(Math.min(value * 5, 1), 2);
        if (flag.getChaseTarget() > 0 && !flag.settled() && progress == 1) {
            float wiggleProgress = (value - 0.2f) / 0.8f;
            progress += (float) (Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8.0f / Math.max(
                1,
                8.0f * wiggleProgress
            ));
        }
        state.progress = progress;
        String filter = be.addressFilter;
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
    }

    @Override
    public void submit(
        PostboxRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.name != null) {
            state.name.submit(matrices, queue, cameraState);
        }
        Pose pose = matrices.last();
        if (state.yRot != 0) {
            pose.rotateYCentered(Mth.DEG_TO_RAD * state.yRot);
        }
        if (state.progress != 0) {
            pose.rotateXAround(-Radians.DEG_90 * state.progress, 0, 0.625f, 0.125f);
        }
        state.flag.submit(matrices, queue);
    }

    public static class PostboxRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState flag;
        public float yRot;
        public float progress;
        public @Nullable NameplateRenderState name;
    }
}
