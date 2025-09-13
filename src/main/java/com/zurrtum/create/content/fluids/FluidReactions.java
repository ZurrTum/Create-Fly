package com.zurrtum.create.content.fluids;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidReactions {
    public static void handlePipeFlowCollision(World level, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
        Fluid f1 = fluid.getFluid();
        Fluid f2 = fluid2.getFluid();

        AdvancementBehaviour.tryAward(level, pos, AllAdvancements.CROSS_STREAMS);
        BlockHelper.destroyBlock(level, pos, 1);

        BlockState state = AllFlowCollision.Flow.get(new AllFlowCollision.FlowEntry(f1, f2));
        if (state != null) {
            level.setBlockState(pos, state);
        }
    }

    public static void handlePipeSpillCollision(World level, BlockPos pos, Fluid pipeFluid, FluidState worldFluid) {
        Fluid pf = FluidHelper.convertToStill(pipeFluid);
        Fluid wf = worldFluid.getFluid();

        BlockState state = AllFlowCollision.Spill.get(new AllFlowCollision.SpillEntry(wf, pf));
        if (state != null) {
            level.setBlockState(pos, state);
        }
    }
}
