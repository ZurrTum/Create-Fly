package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public abstract class AbstractShaftBlock extends RotatedPillarKineticBlock implements IBE<KineticBlockEntity>, ProperWaterloggedBlock {

    public AbstractShaftBlock(Settings properties) {
        super(properties);
        setDefaultState(super.getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(Properties.WATERLOGGED));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        Random random
    ) {
        updateWater(world, tickView, state, pos);
        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return withWater(super.getPlacementState(context), context);
    }

}
