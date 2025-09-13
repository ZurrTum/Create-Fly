package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class BogeyBlockEntityRenderer<T extends AbstractBogeyBlockEntity> extends SafeBlockEntityRenderer<T> {
    public BogeyBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(T be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState blockState = be.getCachedState();
        if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey)) {
            return;
        }

        float angle = be.getVirtualAngle(partialTicks);
        ms.push();
        ms.translate(.5f, .5f, .5f);
        if (blockState.get(AbstractBogeyBlock.AXIS) == Direction.Axis.X)
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        AllBogeyStyleRenders.render(be.getStyle(), bogey.getSize(), partialTicks, ms, buffer, light, overlay, angle, be.getBogeyData(), false);
        ms.pop();
    }
}
