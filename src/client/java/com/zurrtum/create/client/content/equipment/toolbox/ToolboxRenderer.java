package com.zurrtum.create.client.content.equipment.toolbox;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class ToolboxRenderer extends SmartBlockEntityRenderer<ToolboxBlockEntity> {

    public ToolboxRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        ToolboxBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        BlockState blockState = blockEntity.getCachedState();
        Direction facing = blockState.get(ToolboxBlock.FACING).getOpposite();
        SuperByteBuffer lid = CachedBuffers.partial(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor()), blockState);
        SuperByteBuffer drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, blockState);

        float lidAngle = blockEntity.lid.getValue(partialTicks);
        float drawerOffset = blockEntity.drawers.getValue(partialTicks);

        VertexConsumer builder = buffer.getBuffer(RenderLayer.getCutoutMipped());
        lid.center().rotateYDegrees(-facing.getPositiveHorizontalDegrees()).uncenter().translate(0, 6 / 16f, 12 / 16f).rotateXDegrees(135 * lidAngle)
            .translate(0, -6 / 16f, -12 / 16f).light(light).renderInto(ms, builder);

        for (int offset : Iterate.zeroAndOne) {
            drawer.center().rotateYDegrees(-facing.getPositiveHorizontalDegrees()).uncenter()
                .translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset)).light(light).renderInto(ms, builder);
        }

    }

}
