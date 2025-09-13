package com.zurrtum.create.content.trains.observer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class TrackObserverBlock extends Block implements IBE<TrackObserverBlockEntity>, IWrenchable, RedStoneConnectBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public TrackObserverBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(POWERED));
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        return true;
    }

    @Override
    public Class<TrackObserverBlockEntity> getBlockEntityClass() {
        return TrackObserverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrackObserverBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK_OBSERVER;
    }
}
