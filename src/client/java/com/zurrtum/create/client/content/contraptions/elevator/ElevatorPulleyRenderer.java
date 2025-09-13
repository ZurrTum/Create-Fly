package com.zurrtum.create.client.content.contraptions.elevator;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElevatorPulleyRenderer extends KineticBlockEntityRenderer<ElevatorPulleyBlockEntity> {

    public ElevatorPulleyRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        ElevatorPulleyBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        float offset = PulleyRenderer.getBlockEntityOffset(partialTicks, be);
        boolean running = PulleyRenderer.isPulleyRunning(be);

        SpriteShiftEntry beltShift = AllSpriteShifts.ELEVATOR_BELT;
        SpriteShiftEntry coilShift = AllSpriteShifts.ELEVATOR_COIL;
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
        World world = be.getWorld();
        BlockState blockState = be.getCachedState();
        BlockPos pos = be.getPos();

        float blockStateAngle = 180 + AngleHelper.horizontalAngle(blockState.get(ElevatorPulleyBlock.HORIZONTAL_FACING));

        SuperByteBuffer magnet = CachedBuffers.partial(AllPartialModels.ELEVATOR_MAGNET, blockState);
        if (running || offset == 0)
            AbstractPulleyRenderer.renderAt(world, magnet.center().rotateYDegrees(blockStateAngle).uncenter(), offset, pos, ms, vb);

        SuperByteBuffer rotatedCoil = getRotatedCoil(be);
        if (offset == 0) {
            rotatedCoil.light(light).renderInto(ms, vb);
            return;
        }

        AbstractPulleyRenderer.scrollCoil(rotatedCoil, coilShift, offset, 2).light(light).renderInto(ms, vb);

        float spriteSize = beltShift.getTarget().getMaxV() - beltShift.getTarget().getMinV();

        double beltScroll = (-(offset + .5) - Math.floor(-(offset + .5))) / 2;
        SuperByteBuffer halfRope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT_HALF, blockState);
        SuperByteBuffer rope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT, blockState);

        float f = offset % 1;
        if (f < .25f || f > .75f) {
            halfRope.center().rotateYDegrees(blockStateAngle).uncenter();
            AbstractPulleyRenderer.renderAt(
                world,
                halfRope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize),
                f > .75f ? f - 1 : f,
                pos,
                ms,
                vb
            );
        }

        if (!running)
            return;

        for (int i = 0; i < offset - .25f; i++) {
            rope.center().rotateYDegrees(blockStateAngle).uncenter();
            AbstractPulleyRenderer.renderAt(world, rope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize), offset - i, pos, ms, vb);
        }
    }

    @Override
    protected BlockState getRenderedBlockState(ElevatorPulleyBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    protected SuperByteBuffer getRotatedCoil(KineticBlockEntity be) {
        BlockState blockState = be.getCachedState();
        return CachedBuffers.partialFacing(AllPartialModels.ELEVATOR_COIL, blockState, blockState.get(ElevatorPulleyBlock.HORIZONTAL_FACING));
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

}
