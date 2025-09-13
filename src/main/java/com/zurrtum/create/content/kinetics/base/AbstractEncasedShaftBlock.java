package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.WorldView;

public abstract class AbstractEncasedShaftBlock extends RotatedPillarKineticBlock implements WeakPowerControlBlock {
    public AbstractEncasedShaftBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking())
            return super.getPlacementState(context);
        Direction.Axis preferredAxis = getPreferredAxis(context);
        return getDefaultState().with(AXIS, preferredAxis == null ? context.getPlayerLookDirection().getAxis() : preferredAxis);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }
}
