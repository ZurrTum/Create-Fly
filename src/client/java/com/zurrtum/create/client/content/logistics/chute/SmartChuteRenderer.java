package com.zurrtum.create.client.content.logistics.chute;

import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class SmartChuteRenderer extends SmartBlockEntityRenderer<SmartChuteBlockEntity> {

    public SmartChuteRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        SmartChuteBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
        if (blockEntity.getItem().isEmpty())
            return;
        if (blockEntity.itemPosition.getValue(partialTicks) > 0)
            return;
        ChuteRenderer.renderItem(blockEntity, partialTicks, ms, buffer, light, overlay);
    }

}
