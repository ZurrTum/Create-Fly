package com.zurrtum.create.foundation.blockEntity;

import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class ComparatorUtil {

    public static int fractionToRedstoneLevel(double frac) {
        return MathHelper.floor(MathHelper.clamp(frac * 14 + (frac > 0 ? 1 : 0), 0, 15));
    }

    public static int levelOfSmartFluidTank(BlockView world, BlockPos pos) {
        SmartFluidTankBehaviour fluidBehaviour = BlockEntityBehaviour.get(world, pos, SmartFluidTankBehaviour.TYPE);
        if (fluidBehaviour == null)
            return 0;
        TankSegment primaryHandler = fluidBehaviour.getPrimaryHandler();
        double fillFraction = (double) primaryHandler.getFluid().getAmount() / primaryHandler.getMaxAmountPerStack();
        return fractionToRedstoneLevel(fillFraction);
    }

}
