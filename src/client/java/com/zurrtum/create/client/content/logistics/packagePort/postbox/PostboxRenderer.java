package com.zurrtum.create.client.content.logistics.packagePort.postbox;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.NameplateRenderState;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PostboxRenderer implements BlockEntityRenderer<PostboxBlockEntity, PostboxRenderer.PostboxRenderState> {
    public PostboxRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public PostboxRenderState createRenderState() {
        return new PostboxRenderState();
    }

    @Override
    public void updateRenderState(
        PostboxBlockEntity be,
        PostboxRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutout();
        state.flag = CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, state.blockState);
        state.angle = MathHelper.RADIANS_PER_DEGREE * (180 - state.blockState.get(PostboxBlock.FACING).getPositiveHorizontalDegrees());
        LerpedFloat flag = be.flag;
        float value = flag.getValue(tickProgress);
        float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
        if (flag.getChaseTarget() > 0 && !flag.settled() && progress == 1) {
            float wiggleProgress = (value - .2f) / .8f;
            progress += (float) ((Math.sin(wiggleProgress * (2 * MathHelper.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress));
        }
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (-progress * 90);
        String filter = be.addressFilter;
        if (filter != null && !filter.isBlank()) {
            state.name = SmartBlockEntityRenderer.getNameplateRenderState(
                be,
                state.pos,
                cameraPos,
                Text.literal(filter),
                1,
                state.lightmapCoordinates
            );
        }
    }

    @Override
    public void render(PostboxRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        if (state.name != null) {
            state.name.render(matrices, queue, cameraState);
        }
    }

    public static class PostboxRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer flag;
        public float angle;
        public float xRot;
        public NameplateRenderState name;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            flag.light(lightmapCoordinates).overlay(OverlayTexture.DEFAULT_UV).rotateYCentered(angle).translate(0, 0.625f, 0.125f).rotateX(xRot)
                .translate(0, -0.625f, -0.125f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
