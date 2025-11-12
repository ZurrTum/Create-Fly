package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class RedstoneContactBlock extends WrenchableDirectionalBlock implements RedStoneConnectBlock, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneContactBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
        Direction placeDirection = context.getClickedFace().getOpposite();

        if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || hasValidContact(
            context.getLevel(),
            context.getClickedPos(),
            placeDirection
        ))
            state = state.setValue(FACING, placeDirection);
        if (hasValidContact(context.getLevel(), context.getClickedPos(), state.getValue(FACING)))
            state = state.setValue(POWERED, true);

        return state;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        InteractionResult onWrenched = super.onWrenched(state, context);
        if (onWrenched != InteractionResult.SUCCESS)
            return onWrenched;

        Level level = context.getLevel();
        if (level.isClientSide())
            return onWrenched;

        BlockPos pos = context.getClickedPos();
        state = level.getBlockState(pos);
        Direction facing = state.getValue(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return onWrenched;
        if (ElevatorColumn.get(level, new ColumnCoords(pos.getX(), pos.getZ(), facing)) == null)
            return onWrenched;

        level.setBlockAndUpdate(pos, BlockHelper.copyProperties(state, AllBlocks.ELEVATOR_CONTACT.defaultBlockState()));

        return onWrenched;
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.is(this) && oldState == state.cycle(POWERED)) {
            world.updateNeighborsAt(pos, this, null);
        }
    }

    @Override
    public BlockState updateShape(
        BlockState stateIn,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        RandomSource random
    ) {
        if (facing != stateIn.getValue(FACING))
            return stateIn;
        boolean hasValidContact = hasValidContact(world, currentPos, facing);
        if (stateIn.getValue(POWERED) != hasValidContact)
            return stateIn.setValue(POWERED, hasValidContact);
        return stateIn;
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        boolean hasValidContact = hasValidContact(worldIn, pos, state.getValue(FACING));
        if (state.getValue(POWERED) != hasValidContact)
            worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, hasValidContact));
    }

    public static boolean hasValidContact(LevelReader world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos.relative(direction));
        return (blockState.is(AllBlocks.REDSTONE_CONTACT) || blockState.is(AllBlocks.ELEVATOR_CONTACT)) && blockState.getValue(FACING) == direction.getOpposite();
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        return side != null && state.getValue(FACING) != side.getOpposite();
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return state.getValue(POWERED) && side != state.getValue(FACING).getOpposite() ? 15 : 0;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }
}
