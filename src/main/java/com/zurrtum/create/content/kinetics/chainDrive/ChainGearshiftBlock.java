package com.zurrtum.create.content.kinetics.chainDrive;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class ChainGearshiftBlock extends ChainDriveBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public ChainGearshiftBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED));
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        if (oldState.getBlock() == state.getBlock())
            return;
        withBlockEntityDo(worldIn, pos, kbe -> ((ChainGearshiftBlockEntity) kbe).neighbourChanged());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return super.getPlacementState(context).with(POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()));
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return super.areStatesKineticallyEquivalent(oldState, newState) && oldState.get(POWERED) == newState.get(POWERED);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient())
            return;

        withBlockEntityDo(worldIn, pos, kbe -> ((ChainGearshiftBlockEntity) kbe).neighbourChanged());

        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos))
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT;
    }

}
