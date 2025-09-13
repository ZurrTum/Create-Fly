package com.zurrtum.create.client.content.logistics.packagePort.postbox;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.transform.Transform;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class PostboxRenderer extends SmartBlockEntityRenderer<PostboxBlockEntity> {

    public PostboxRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        PostboxBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        if (blockEntity.addressFilter != null && !blockEntity.addressFilter.isBlank()) {
            renderNameplateOnHover(blockEntity, Text.literal(blockEntity.addressFilter), 1, ms, buffer, light);
        }

        SuperByteBuffer sbb = CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, blockEntity.getCachedState());

        sbb.light(light).overlay(overlay)
            .rotateCentered(
                MathHelper.RADIANS_PER_DEGREE * (180 - blockEntity.getCachedState().get(PostboxBlock.FACING).getPositiveHorizontalDegrees()),
                RotationAxis.POSITIVE_Y
            );

        transformFlag(sbb, blockEntity, partialTicks);

        sbb.renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
    }

    public static void transformFlag(Transform<?> flag, PostboxBlockEntity be, float partialTicks) {
        float value = be.flag.getValue(partialTicks);
        float progress = (float) (Math.pow(Math.min(value * 5, 1), 2));
        if (be.flag.getChaseTarget() > 0 && !be.flag.settled() && progress == 1) {
            float wiggleProgress = (value - .2f) / .8f;
            progress += (Math.sin(wiggleProgress * (2 * MathHelper.PI) * 4) / 8f) / Math.max(1, 8f * wiggleProgress);
        }
        flag.translate(0, 10 / 16f, 2 / 16f);
        flag.rotateXDegrees(-progress * 90);
        flag.translateBack(0, 10 / 16f, 2 / 16f);
    }

}
