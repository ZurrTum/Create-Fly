package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public abstract class ColoredOverlayBlockEntityRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {

    public ColoredOverlayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        SuperByteBuffer render = render(getOverlayBuffer(be), getColor(be, partialTicks), light);
        render.renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

    protected abstract int getColor(T be, float partialTicks);

    protected abstract SuperByteBuffer getOverlayBuffer(T be);

    public static SuperByteBuffer render(SuperByteBuffer buffer, int color, int light) {
        return buffer.color(color).light(light);
    }

}
