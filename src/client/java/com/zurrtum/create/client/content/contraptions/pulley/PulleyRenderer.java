package com.zurrtum.create.client.content.contraptions.pulley;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.contraptions.pulley.PulleyContraption;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class PulleyRenderer extends AbstractPulleyRenderer<PulleyBlockEntity> {

    public PulleyRenderer(BlockEntityRendererFactory.Context context) {
        super(context, AllPartialModels.ROPE_HALF, AllPartialModels.ROPE_HALF_MAGNET);
    }

    @Override
    protected Axis getShaftAxis(PulleyBlockEntity be) {
        return be.getCachedState().get(PulleyBlock.HORIZONTAL_AXIS);
    }

    @Override
    protected PartialModel getCoil() {
        return AllPartialModels.ROPE_COIL;
    }

    @Override
    protected SuperByteBuffer renderRope(PulleyBlockEntity be) {
        return CachedBuffers.block(AllBlocks.ROPE.getDefaultState());
    }

    @Override
    protected SuperByteBuffer renderMagnet(PulleyBlockEntity be) {
        return CachedBuffers.block(AllBlocks.PULLEY_MAGNET.getDefaultState());
    }

    @Override
    protected float getOffset(PulleyBlockEntity be, float partialTicks) {
        return getBlockEntityOffset(partialTicks, be);
    }

    @Override
    protected boolean isRunning(PulleyBlockEntity be) {
        return isPulleyRunning(be);
    }

    public static boolean isPulleyRunning(PulleyBlockEntity be) {
        return be.running || be.getMirrorParent() != null || be.isVirtual();
    }

    @Override
    protected SpriteShiftEntry getCoilShift() {
        return AllSpriteShifts.ROPE_PULLEY_COIL;
    }

    public static float getBlockEntityOffset(float partialTicks, PulleyBlockEntity blockEntity) {
        float offset = blockEntity.getInterpolatedOffset(partialTicks);

        AbstractContraptionEntity attachedContraption = blockEntity.getAttachedContraption();
        if (attachedContraption != null) {
            PulleyContraption c = (PulleyContraption) attachedContraption.getContraption();
            double entityPos = MathHelper.lerp(partialTicks, attachedContraption.lastRenderY, attachedContraption.getY());
            offset = (float) -(entityPos - c.anchor.getY() - c.getInitialOffset());
        }

        return offset;
    }
}
