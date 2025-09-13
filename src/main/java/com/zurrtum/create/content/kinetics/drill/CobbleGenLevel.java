package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

import java.util.HashMap;

public class CobbleGenLevel extends WrappedLevel {

    public HashMap<BlockPos, BlockState> blocksAdded = new HashMap<>();

    public CobbleGenLevel(World level) {
        super(level);
    }

    public void clear() {
        blocksAdded.clear();
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        blocksAdded.put(pos.toImmutable(), newState);
        return true;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state) {
        return setBlockState(pos, state, 0);
    }

    @Override
    public void scheduleBlockTick(BlockPos pos, Block block, int delay) {
    }

    @Override
    public void scheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority) {
    }

    @Override
    public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay) {
    }

    @Override
    public void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
    }

    @Override
    public void syncWorldEvent(int type, BlockPos pos, int data) {
    }

    @Override
    public void syncWorldEvent(Entity player, int type, BlockPos pos, int data) {
    }

    @Override
    public void addSyncedBlockEvent(BlockPos pos, Block block, int eventID, int eventParam) {
    }

}
