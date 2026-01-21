package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

public class CobbleGenLevel extends WrappedLevel {

    public HashMap<BlockPos, BlockState> blocksAdded = new HashMap<>();

    public CobbleGenLevel(Level level) {
        super(level);
    }

    public void clear() {
        blocksAdded.clear();
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        blocksAdded.put(pos.immutable(), newState);
        return true;
    }

    @Override
    public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
        return setBlock(pos, state, 0);
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
    }

    @Override
    public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
    }

    @Override
    public void levelEvent(int type, BlockPos pos, int data) {
    }

    @Override
    public void levelEvent(@Nullable Entity player, int type, BlockPos pos, int data) {
    }

    @Override
    public void blockEvent(BlockPos pos, Block block, int eventID, int eventParam) {
    }

}
