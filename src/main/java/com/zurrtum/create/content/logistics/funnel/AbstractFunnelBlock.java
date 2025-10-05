package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFunnelBlock extends Block implements IBE<FunnelBlockEntity>, IWrenchable, ProperWaterloggedBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    protected AbstractFunnelBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
        setDefaultState(getDefaultState().with(POWERED, false).with(WATERLOGGED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return withWater(getDefaultState().with(POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos())), context);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED, WATERLOGGED));
    }

    @Override
    public void neighborUpdate(BlockState state, World level, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (level.isClient())
            return;
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(level, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World level,
        BlockPos pos,
        Block block,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (level.isClient())
            return;
        if (!level.getBlockTickScheduler().isTicking(pos, this))
            level.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos))
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
    }

    public static ItemStack tryInsert(World worldIn, BlockPos pos, ItemStack toInsert, boolean simulate) {
        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(worldIn, pos, ServerFilteringBehaviour.TYPE);
        InvManipulationBehaviour inserter = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (inserter == null)
            return toInsert;
        if (filter != null && !filter.test(toInsert))
            return toInsert;
        if (simulate)
            inserter.simulate();
        ItemStack insert = inserter.insert(toInsert);

        if (!simulate && insert.getCount() != toInsert.getCount()) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof FunnelBlockEntity funnelBlockEntity) {
                funnelBlockEntity.onTransfer(toInsert);
                if (funnelBlockEntity.hasFlap())
                    funnelBlockEntity.flap(true);
            }
        }
        return insert;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Block block = world.getBlockState(pos.offset(getFunnelFacing(state).getOpposite())).getBlock();
        return !(block instanceof AbstractFunnelBlock);
    }

    public static boolean isFunnel(BlockState state) {
        return state.getBlock() instanceof AbstractFunnelBlock;
    }

    @Nullable
    public static Direction getFunnelFacing(BlockState state) {
        if (!(state.getBlock() instanceof AbstractFunnelBlock))
            return null;
        return ((AbstractFunnelBlock) state.getBlock()).getFacing(state);
    }

    protected abstract Direction getFacing(BlockState state);

    @Override
    public Class<FunnelBlockEntity> getBlockEntityClass() {
        return FunnelBlockEntity.class;
    }

    public BlockEntityType<? extends FunnelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.FUNNEL;
    }
}
