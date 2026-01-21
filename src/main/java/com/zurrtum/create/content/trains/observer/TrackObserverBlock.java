package com.zurrtum.create.content.trains.observer;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

public class TrackObserverBlock extends Block implements IBE<TrackObserverBlockEntity>, IWrenchable, RedStoneConnectBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public TrackObserverBlock(Properties p_49795_) {
        super(p_49795_);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(POWERED));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
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
