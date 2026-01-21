package com.zurrtum.create.content.redstone.smartObserver;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

public class SmartObserverBlock extends DirectedDirectionalBlock implements IBE<SmartObserverBlockEntity>, RedStoneConnectBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SmartObserverBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(POWERED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockPos offsetPos = context.getClickedPos().relative(face);
            Level world = context.getLevel();
            boolean canDetect = false;
            BlockEntity blockEntity = world.getBlockEntity(offsetPos);

            if (BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
                canDetect = true;
            else if (BlockEntityBehaviour.get(blockEntity, FluidTransportBehaviour.TYPE) != null)
                canDetect = true;
            else if (blockEntity != null && (ItemHelper.getInventory(
                context.getLevel(),
                offsetPos,
                null,
                blockEntity,
                null
            ) != null || FluidHelper.hasFluidInventory(
                context.getLevel(),
                offsetPos,
                null,
                blockEntity,
                null
            )))
                canDetect = true;
            else if (blockEntity instanceof FunnelBlockEntity)
                canDetect = true;

            if (canDetect) {
                preferredFacing = face;
                break;
            }
        }

        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? facing : facing.getOpposite();
        }

        if (preferredFacing.getAxis() == Axis.Y) {
            state = state.setValue(TARGET, preferredFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
            preferredFacing = context.getHorizontalDirection();
        }

        return state.setValue(FACING, preferredFacing);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, @Nullable Direction side) {
        return isSignalSource(blockState) && (side == null || side != getTargetDirection(blockState).getOpposite()) ? 15 : 0;
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        worldIn.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_CLIENTS);
        worldIn.updateNeighborsAt(pos, this, null);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        return side != state.getValue(FACING).getOpposite();
    }

    @Override
    public void neighborUpdate(BlockState state, Level worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    public void onFunnelTransfer(Level world, BlockPos funnelPos, ItemStack transferred) {
        for (Direction direction : Iterate.directions) {
            BlockPos detectorPos = funnelPos.relative(direction);
            BlockState detectorState = world.getBlockState(detectorPos);
            if (!detectorState.is(AllBlocks.SMART_OBSERVER))
                continue;
            if (SmartObserverBlock.getTargetDirection(detectorState) != direction.getOpposite())
                continue;
            withBlockEntityDo(
                world, detectorPos, be -> {
                    ServerFilteringBehaviour filteringBehaviour = BlockEntityBehaviour.get(be, ServerFilteringBehaviour.TYPE);
                    if (filteringBehaviour == null)
                        return;
                    if (!filteringBehaviour.test(transferred))
                        return;
                    be.activate(4);
                }
            );
        }
    }

    @Override
    public Class<SmartObserverBlockEntity> getBlockEntityClass() {
        return SmartObserverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmartObserverBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SMART_OBSERVER;
    }

}
