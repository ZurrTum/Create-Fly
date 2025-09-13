package com.zurrtum.create.client.content.logistics.funnel;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class FunnelRenderer extends SmartBlockEntityRenderer<FunnelBlockEntity> {

    public FunnelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FunnelBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (!be.hasFlap() || VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState blockState = be.getCachedState();
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
        PartialModel partialModel = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP : AllPartialModels.BELT_FUNNEL_FLAP);
        SuperByteBuffer flapBuffer = CachedBuffers.partial(partialModel, blockState);

        var funnelFacing = FunnelBlock.getFunnelFacing(blockState);
        float f = be.flap.getValue(partialTicks);

        FlapStuffs.renderFlaps(ms, vb, flapBuffer, FlapStuffs.FUNNEL_PIVOT, funnelFacing, f, -be.getFlapOffset(), light);
    }

}
