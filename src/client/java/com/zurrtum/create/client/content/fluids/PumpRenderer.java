package com.zurrtum.create.client.content.fluids;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class PumpRenderer extends KineticBlockEntityRenderer<PumpBlockEntity> {

    public PumpRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(PumpBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.MECHANICAL_PUMP_COG, state);
    }

}
