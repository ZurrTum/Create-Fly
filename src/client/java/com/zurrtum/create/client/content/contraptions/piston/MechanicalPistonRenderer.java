package com.zurrtum.create.client.content.contraptions.piston;

import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class MechanicalPistonRenderer extends KineticBlockEntityRenderer<MechanicalPistonBlockEntity> {

    public MechanicalPistonRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected BlockState getRenderedBlockState(MechanicalPistonBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
