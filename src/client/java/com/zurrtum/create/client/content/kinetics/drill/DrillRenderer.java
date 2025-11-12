package com.zurrtum.create.client.content.kinetics.drill;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.content.kinetics.drill.DrillBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DrillRenderer extends KineticBlockEntityRenderer<DrillBlockEntity, KineticRenderState> {
    public DrillRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(DrillBlockEntity be, KineticRenderState state) {
        return CachedBuffers.partialFacing(AllPartialModels.DRILL_HEAD, state.blockState);
    }
}
