package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class FluidBlock extends net.minecraft.block.FluidBlock {
    public FluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleFluidTick(pos, state.getFluidState().getFluid(), this.fluid.getTickRate(world));
    }

    @Override
    protected void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block sourceBlock,
        @Nullable WireOrientation wireOrientation,
        boolean notify
    ) {
        world.scheduleFluidTick(pos, state.getFluidState().getFluid(), this.fluid.getTickRate(world));
    }
}
