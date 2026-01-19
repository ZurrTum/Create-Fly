package com.zurrtum.create.client.content.logistics.packagePort.postbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.NameplateRenderState;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PostboxRenderer implements BlockEntityRenderer<PostboxBlockEntity, PostboxRenderer.PostboxRenderState> {
    public PostboxRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderTypes.cutoutMovingBlock();
        state.flag = CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, state.blockState);
        state.angle = Mth.DEG_TO_RAD * (180 - state.blockState.getValue(PostboxBlock.FACING).toYRot());
        LerpedFloat flag = be.flag;
        float value = flag.getValue(tickProgress);
        float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
        if (flag.getChaseTarget() > 0 && !flag.settled() && progress == 1) {
            float wiggleProgress = (value - .2f) / .8f;
            progress += (float) ((Math.sin(wiggleProgress * (2 * Mth.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress));
        }
        state.xRot = Mth.DEG_TO_RAD * (-progress * 90);
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
    public void submit(PostboxRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
        if (state.name != null) {
            state.name.render(matrices, queue, cameraState);
        }
    }

    public static class PostboxRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer flag;
        public float angle;
        public float xRot;
        public NameplateRenderState name;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            flag.light(lightCoords).overlay(OverlayTexture.NO_OVERLAY).rotateYCentered(angle).translate(0, 0.625f, 0.125f).rotateX(xRot)
                .translate(0, -0.625f, -0.125f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
