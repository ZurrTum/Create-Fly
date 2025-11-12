package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class FluidBlock extends net.minecraft.world.level.block.LiquidBlock {
    public FluidBlock(FlowingFluid fluid, Properties settings) {
        super(fluid, settings);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleTick(pos, state.getFluidState().getType(), this.fluid.getTickDelay(world));
    }

    @Override
    protected void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block sourceBlock,
        @Nullable Orientation wireOrientation,
        boolean notify
    ) {
        world.scheduleTick(pos, state.getFluidState().getType(), this.fluid.getTickDelay(world));
    }
}
