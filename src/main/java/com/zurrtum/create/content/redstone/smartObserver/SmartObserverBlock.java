package com.zurrtum.create.content.redstone.smartObserver;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SmartObserverBlock extends DirectedDirectionalBlock implements IBE<SmartObserverBlockEntity>, RedStoneConnectBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public SmartObserverBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState();

        Direction preferredFacing = null;
        for (Direction face : context.getPlacementDirections()) {
            BlockPos offsetPos = context.getBlockPos().offset(face);
            World world = context.getWorld();
            boolean canDetect = false;
            BlockEntity blockEntity = world.getBlockEntity(offsetPos);

            if (BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
                canDetect = true;
            else if (BlockEntityBehaviour.get(blockEntity, FluidTransportBehaviour.TYPE) != null)
                canDetect = true;
            else if (blockEntity != null && (ItemHelper.getInventory(
                context.getWorld(),
                offsetPos,
                null,
                blockEntity,
                null
            ) != null || FluidHelper.hasFluidInventory(
                context.getWorld(),
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
            Direction facing = context.getPlayerLookDirection();
            preferredFacing = context.getPlayer() != null && context.getPlayer().isSneaking() ? facing : facing.getOpposite();
        }

        if (preferredFacing.getAxis() == Axis.Y) {
            state = state.with(TARGET, preferredFacing == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR);
            preferredFacing = context.getHorizontalPlayerFacing();
        }

        return state.with(FACING, preferredFacing);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(POWERED);
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return emitsRedstonePower(blockState) && (side == null || side != getTargetDirection(blockState).getOpposite()) ? 15 : 0;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        worldIn.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
        worldIn.updateNeighborsAlways(pos, this, null);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        return side != state.get(FACING).getOpposite();
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
        if (behaviour != null)
            behaviour.onNeighborChanged(fromPos);
    }

    public void onFunnelTransfer(World world, BlockPos funnelPos, ItemStack transferred) {
        for (Direction direction : Iterate.directions) {
            BlockPos detectorPos = funnelPos.offset(direction);
            BlockState detectorState = world.getBlockState(detectorPos);
            if (!detectorState.isOf(AllBlocks.SMART_OBSERVER))
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
