package com.zurrtum.create.content.redstone.link;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class RedstoneLinkBlock extends WrenchableDirectionalBlock implements IBE<RedstoneLinkBlockEntity>, RedStoneConnectBlock, NeighborUpdateListeningBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty RECEIVER = BooleanProperty.of("receiver");

    public RedstoneLinkBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false).with(RECEIVER, false));
    }

    @Override
    public void neighborUpdate(BlockState state, World level, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (level.isClient())
            return;
        if (fromPos.equals(pos.offset(state.get(FACING).getOpposite()))) {
            if (!canPlaceAt(state, level, pos)) {
                level.breakBlock(pos, true);
                return;
            }
        }
        if (!level.getBlockTickScheduler().isTicking(pos, this))
            level.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld level, BlockPos pos, Random r) {
        updateTransmittedSignal(state, level, pos);

        if (state.get(RECEIVER))
            return;
        Direction attachedFace = state.get(RedstoneLinkBlock.FACING).getOpposite();
        BlockPos attachedPos = pos.offset(attachedFace);
        level.updateNeighbors(pos, level.getBlockState(pos).getBlock());
        level.updateNeighbors(attachedPos, level.getBlockState(attachedPos).getBlock());
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() == oldState.getBlock() || isMoving)
            return;
        updateTransmittedSignal(state, worldIn, pos);
    }

    public void updateTransmittedSignal(BlockState state, World level, BlockPos pos) {
        if (level.isClient())
            return;
        if (state.get(RECEIVER))
            return;

        int power = getPower(level, state, pos);
        int powerFromPanels = getBlockEntityOptional(level, pos).map(be -> {
            if (be.panelSupport == null)
                return 0;
            Boolean tri = be.panelSupport.shouldBePoweredTristate();
            if (tri == null)
                return -1;
            return tri ? 15 : 0;
        }).orElse(0);

        // Suppress update if an input panel exists but is not loaded
        if (powerFromPanels == -1)
            return;

        power = Math.max(power, powerFromPanels);

        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != power > 0)
            level.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);

        int transmit = power;
        withBlockEntityDo(level, pos, be -> be.transmit(transmit));
    }

    private static int getPower(World level, BlockState state, BlockPos pos) {
        int power = 0;
        for (Direction direction : Iterate.directions)
            power = Math.max(level.getEmittedRedstonePower(pos.offset(direction), direction), power);
        for (Direction direction : Iterate.directions) {
            if (state.get(FACING).getOpposite() != direction)
                power = Math.max(level.getEmittedRedstonePower(pos.offset(direction), Direction.UP), power);
        }
        return power;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(POWERED) && state.get(RECEIVER);
    }

    @Override
    public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        if (side != blockState.get(FACING))
            return 0;
        return getWeakRedstonePower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView blockAccess, BlockPos pos, Direction side) {
        if (!state.get(RECEIVER))
            return 0;
        return getBlockEntityOptional(blockAccess, pos).map(RedstoneLinkBlockEntity::getReceivedSignal).orElse(0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, RECEIVER);
        super.appendProperties(builder);
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (player.isSneaking() && toggleMode(state, level, pos) == ActionResult.SUCCESS) {
            level.scheduleBlockTick(pos, this, 1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public ActionResult toggleMode(BlockState state, World level, BlockPos pos) {
        if (level.isClient())
            return ActionResult.SUCCESS;

        return onBlockEntityUse(
            level, pos, be -> {
                Boolean wasReceiver = state.get(RECEIVER);
                boolean blockPowered = level.isReceivingRedstonePower(pos);
                level.setBlockState(pos, state.cycle(RECEIVER).with(POWERED, blockPowered), Block.NOTIFY_ALL);
                be.transmit(wasReceiver ? 0 : getPower(level, state, pos));
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (toggleMode(state, context.getWorld(), context.getBlockPos()) == ActionResult.SUCCESS) {
            context.getWorld().scheduleBlockTick(context.getBlockPos(), this, 1);
            return ActionResult.SUCCESS;
        }
        return super.onWrenched(state, context);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction _targetedFace) {
        return originalState;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        return side != null;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        BlockPos neighbourPos = pos.offset(state.get(FACING).getOpposite());
        BlockState neighbour = worldIn.getBlockState(neighbourPos);
        return !neighbour.isReplaceable();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState();
        state = state.with(FACING, context.getSide());
        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.REDSTONE_BRIDGE.get(state.get(FACING));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public Class<RedstoneLinkBlockEntity> getBlockEntityClass() {
        return RedstoneLinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneLinkBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.REDSTONE_LINK;
    }

}
