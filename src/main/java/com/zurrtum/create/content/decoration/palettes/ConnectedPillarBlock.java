package com.zurrtum.create.content.decoration.palettes;

import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.ScheduledTickView;

public class ConnectedPillarBlock extends LayeredBlock {

    public static final BooleanProperty NORTH = BooleanProperty.of("north");
    public static final BooleanProperty SOUTH = BooleanProperty.of("south");
    public static final BooleanProperty EAST = BooleanProperty.of("east");
    public static final BooleanProperty WEST = BooleanProperty.of("west");

    public ConnectedPillarBlock(Settings p_55926_) {
        super(p_55926_);
        setDefaultState(getDefaultState().with(NORTH, false).with(WEST, false).with(EAST, false).with(SOUTH, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(NORTH, SOUTH, EAST, WEST));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState state = super.getPlacementState(pContext);
        return updateColumn(pContext.getWorld(), pContext.getBlockPos(), state, true);
    }

    private BlockState updateColumn(World level, BlockPos pos, BlockState state, boolean present) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        Axis axis = state.get(AXIS);

        for (Direction connection : Iterate.directions) {
            if (connection.getAxis() == axis)
                continue;

            boolean connect = true;
            Move:
            for (Direction movement : Iterate.directionsInAxis(axis)) {
                currentPos.set(pos);
                for (int i = 0; i < 1000; i++) {
                    if (!level.isPosLoaded(currentPos))
                        break;

                    BlockState other1 = currentPos.equals(pos) ? state : level.getBlockState(currentPos);
                    BlockState other2 = level.getBlockState(currentPos.offset(connection));
                    boolean col1 = canConnect(state, other1);
                    boolean col2 = canConnect(state, other2);
                    currentPos.move(movement);

                    if (!col1 && !col2)
                        break;
                    if (col1 && col2)
                        continue;

                    connect = false;
                    break Move;
                }
            }
            state = setConnection(state, connection, connect);
        }
        return state;
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() == this)
            return;
        QueryableTickScheduler<Block> blockTicks = pLevel.getBlockTickScheduler();
        if (!blockTicks.isQueued(pPos, this))
            pLevel.scheduleBlockTick(pPos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (pState.getBlock() != this)
            return;
        BlockPos belowPos = pPos.offset(Direction.from(pState.get(AXIS), AxisDirection.NEGATIVE));
        BlockState belowState = pLevel.getBlockState(belowPos);
        if (!canConnect(pState, belowState))
            pLevel.setBlockState(pPos, updateColumn(pLevel, pPos, pState, true), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        if (!canConnect(state, pNeighborState))
            return setConnection(state, pDirection, false);
        if (pDirection.getAxis() == state.get(AXIS))
            return getStateWithProperties(pNeighborState);

        return setConnection(state, pDirection, getConnection(pNeighborState, pDirection.getOpposite()));
    }

    protected boolean canConnect(BlockState state, BlockState other) {
        return other.getBlock() == this && state.get(AXIS) == other.get(AXIS);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        if (pIsMoving)
            return;
        for (Direction d : Iterate.directionsInAxis(pState.get(AXIS))) {
            BlockPos relative = pPos.offset(d);
            BlockState adjacent = pLevel.getBlockState(relative);
            if (canConnect(pState, adjacent))
                pLevel.setBlockState(relative, updateColumn(pLevel, relative, adjacent, false), Block.NOTIFY_ALL);
        }
    }

    public static boolean getConnection(BlockState state, Direction side) {
        BooleanProperty property = connection(state.get(AXIS), side);
        return property != null && state.get(property);
    }

    public static BlockState setConnection(BlockState state, Direction side, boolean connect) {
        BooleanProperty property = connection(state.get(AXIS), side);
        if (property != null)
            state = state.with(property, connect);
        return state;
    }

    public static BooleanProperty connection(Axis axis, Direction side) {
        if (side.getAxis() == axis)
            return null;

        if (axis == Axis.X) {
            return switch (side) {
                case UP -> EAST;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case DOWN -> WEST;
                default -> null;
            };
        }

        if (axis == Axis.Y) {
            return switch (side) {
                case EAST -> EAST;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                default -> null;
            };
        }

        if (axis == Axis.Z) {
            return switch (side) {
                case UP -> WEST;
                case WEST -> SOUTH;
                case EAST -> NORTH;
                case DOWN -> EAST;
                default -> null;
            };
        }

        return null;
    }

}