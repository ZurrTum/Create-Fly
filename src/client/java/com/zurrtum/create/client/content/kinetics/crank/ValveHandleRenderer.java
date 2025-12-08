package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ValveHandleRenderer extends HandCrankRenderer {
    public ValveHandleRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public float getIndependentAngle(HandCrankBlockEntity be, float partialTicks) {
        return getValveHandleIndependentAngle((ValveHandleBlockEntity) be, partialTicks);
    }

    public static float getValveHandleIndependentAngle(ValveHandleBlockEntity be, float partialTicks) {
        if (be.inUse == 0 && be.source != null && be.getSpeed() != 0)
            return AngleHelper.deg(KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be)));

        int step = be.getBlockState().getOptionalValue(ValveHandleBlock.FACING).orElse(Direction.SOUTH).getAxisDirection().getStep();

        return (be.inUse > 0 && be.totalUseTicks > 0 ? Mth.lerpInt(
            Math.min(
                be.totalUseTicks,
                be.totalUseTicks - be.inUse + partialTicks
            ) / (float) be.totalUseTicks,
            be.startAngle,
            be.targetAngle
        ) : be.targetAngle) * (be.backwards ? -1 : 1) * step;
    }

    @Override
    public SuperByteBuffer getRenderedHandle(BlockState blockState) {
        return CachedBuffers.block(blockState);
    }

    @Override
    public boolean shouldRenderShaft() {
        return false;
    }
}
