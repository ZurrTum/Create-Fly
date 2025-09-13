package com.zurrtum.create.foundation.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

/**
 * Waterlog checklist: <br>
 * 1. createBlockStateDefinition -> add WATERLOGGED <br>
 * 2. constructor -> default WATERLOGGED to false <br>
 * 3. getFluidState -> return fluidState <br>
 * 4. getStateForPlacement -> call withWater <br>
 * 5. updateShape -> call updateWater
 */
public interface ProperWaterloggedBlock extends Waterloggable {

    BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    default FluidState fluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    default void updateWater(WorldView level, ScheduledTickView tickView, BlockState state, BlockPos pos) {
        if (state.get(Properties.WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(level));
    }

    default BlockState withWater(BlockState placementState, ItemPlacementContext ctx) {
        return withWater(ctx.getWorld(), placementState, ctx.getBlockPos());
    }

    static BlockState withWater(WorldView level, BlockState placementState, BlockPos pos) {
        if (placementState == null)
            return null;
        FluidState ifluidstate = level.getFluidState(pos);
        if (placementState.isAir())
            return ifluidstate.getFluid() == Fluids.WATER ? ifluidstate.getBlockState() : placementState;
        if (!(placementState.getBlock() instanceof Waterloggable))
            return placementState;
        return placementState.with(Properties.WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
    }

}