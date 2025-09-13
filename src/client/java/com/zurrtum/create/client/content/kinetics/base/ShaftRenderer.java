package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class ShaftRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {

    public ShaftRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected BlockState getRenderedBlockState(KineticBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
