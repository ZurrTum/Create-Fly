package com.zurrtum.create.client.content.redstone.diodes;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.ColoredOverlayBlockEntityRenderer;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class BrassDiodeRenderer extends ColoredOverlayBlockEntityRenderer<BrassDiodeBlockEntity> {

    public BrassDiodeRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected int getColor(BrassDiodeBlockEntity be, float partialTicks) {
        return Color.mixColors(0x2C0300, 0xCD0000, be.getProgress());
    }

    @Override
    protected SuperByteBuffer getOverlayBuffer(BrassDiodeBlockEntity be) {
        return CachedBuffers.partial(AllPartialModels.FLEXPEATER_INDICATOR, be.getCachedState());
    }

}
