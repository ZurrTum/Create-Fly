package com.zurrtum.create.client.content.fluids.hosePulley;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlock;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.math.Direction.Axis;

public class HosePulleyRenderer extends AbstractPulleyRenderer<HosePulleyBlockEntity> {

    public HosePulleyRenderer(BlockEntityRendererFactory.Context context) {
        super(context, AllPartialModels.HOSE_HALF, AllPartialModels.HOSE_HALF_MAGNET);
    }

    @Override
    protected Axis getShaftAxis(HosePulleyBlockEntity be) {
        return be.getCachedState().get(HosePulleyBlock.HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    @Override
    protected PartialModel getCoil() {
        return AllPartialModels.HOSE_COIL;
    }

    @Override
    protected SuperByteBuffer renderRope(HosePulleyBlockEntity be) {
        return CachedBuffers.partial(AllPartialModels.HOSE, be.getCachedState());
    }

    @Override
    protected SuperByteBuffer renderMagnet(HosePulleyBlockEntity be) {
        return CachedBuffers.partial(AllPartialModels.HOSE_MAGNET, be.getCachedState());
    }

    @Override
    protected float getOffset(HosePulleyBlockEntity be, float partialTicks) {
        return be.getInterpolatedOffset(partialTicks);
    }

    @Override
    protected SpriteShiftEntry getCoilShift() {
        return AllSpriteShifts.HOSE_PULLEY_COIL;
    }

    @Override
    protected boolean isRunning(HosePulleyBlockEntity be) {
        return true;
    }

}
