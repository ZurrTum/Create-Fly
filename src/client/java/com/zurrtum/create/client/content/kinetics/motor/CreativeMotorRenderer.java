package com.zurrtum.create.client.content.kinetics.motor;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CreativeMotorRenderer extends KineticBlockEntityRenderer<CreativeMotorBlockEntity, KineticRenderState> {
    public CreativeMotorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CreativeMotorBlockEntity be, KineticRenderState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState);
    }
}
