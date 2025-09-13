package com.zurrtum.create.client.content.kinetics.millstone;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class MillstoneRenderer extends KineticBlockEntityRenderer<MillstoneBlockEntity> {

    public MillstoneRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MillstoneBlockEntity be, BlockState state) {
        return CachedBuffers.partial(AllPartialModels.MILLSTONE_COG, state);
    }

}
