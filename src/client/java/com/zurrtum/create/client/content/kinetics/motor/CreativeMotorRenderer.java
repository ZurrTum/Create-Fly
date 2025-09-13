package com.zurrtum.create.client.content.kinetics.motor;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class CreativeMotorRenderer extends KineticBlockEntityRenderer<CreativeMotorBlockEntity> {

    public CreativeMotorRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CreativeMotorBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
    }

}
