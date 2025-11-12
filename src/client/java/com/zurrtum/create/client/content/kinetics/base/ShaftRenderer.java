package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ShaftRenderer<T extends KineticBlockEntity, S extends KineticRenderState> extends KineticBlockEntityRenderer<T, S> {
    public ShaftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected BlockState getRenderedBlockState(KineticBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
