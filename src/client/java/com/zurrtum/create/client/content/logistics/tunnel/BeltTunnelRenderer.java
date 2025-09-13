package com.zurrtum.create.client.content.logistics.tunnel;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class BeltTunnelRenderer extends SmartBlockEntityRenderer<BeltTunnelBlockEntity> {

    public BeltTunnelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BeltTunnelBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        SuperByteBuffer flapBuffer = CachedBuffers.partial(AllPartialModels.BELT_TUNNEL_FLAP, be.getCachedState());
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        for (Direction direction : Iterate.directions) {
            if (!be.flaps.containsKey(direction))
                continue;

            float f = be.flaps.get(direction).getValue(partialTicks);

            FlapStuffs.renderFlaps(ms, vb, flapBuffer, FlapStuffs.TUNNEL_PIVOT, direction, f, 0, light);
        }

    }

}
