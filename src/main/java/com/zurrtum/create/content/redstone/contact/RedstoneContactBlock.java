package com.zurrtum.create.content.redstone.contact;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class RedstoneContactBlock extends WrenchableDirectionalBlock implements RedStoneConnectBlock, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public RedstoneContactBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false).with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState().with(FACING, context.getPlayerLookDirection().getOpposite());
        Direction placeDirection = context.getSide().getOpposite();

        if ((context.getPlayer() != null && context.getPlayer().isSneaking()) || hasValidContact(
            context.getWorld(),
            context.getBlockPos(),
            placeDirection
        ))
            state = state.with(FACING, placeDirection);
        if (hasValidContact(context.getWorld(), context.getBlockPos(), state.get(FACING)))
            state = state.with(POWERED, true);

        return state;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult onWrenched = super.onWrenched(state, context);
        if (onWrenched != ActionResult.SUCCESS)
            return onWrenched;

        World level = context.getWorld();
        if (level.isClient())
            return onWrenched;

        BlockPos pos = context.getBlockPos();
        state = level.getBlockState(pos);
        Direction facing = state.get(RedstoneContactBlock.FACING);
        if (facing.getAxis() == Axis.Y)
            return onWrenched;
        if (ElevatorColumn.get(level, new ColumnCoords(pos.getX(), pos.getZ(), facing)) == null)
            return onWrenched;

        level.setBlockState(pos, BlockHelper.copyProperties(state, AllBlocks.ELEVATOR_CONTACT.getDefaultState()));

        return onWrenched;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(this) && oldState == state.cycle(POWERED)) {
            world.updateNeighborsAlways(pos, this, null);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState stateIn,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        if (facing != stateIn.get(FACING))
            return stateIn;
        boolean hasValidContact = hasValidContact(world, currentPos, facing);
        if (stateIn.get(POWERED) != hasValidContact)
            return stateIn.with(POWERED, hasValidContact);
        return stateIn;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        boolean hasValidContact = hasValidContact(worldIn, pos, state.get(FACING));
        if (state.get(POWERED) != hasValidContact)
            worldIn.setBlockState(pos, state.with(POWERED, hasValidContact));
    }

    public static boolean hasValidContact(WorldView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos.offset(direction));
        return (blockState.isOf(AllBlocks.REDSTONE_CONTACT) || blockState.isOf(AllBlocks.ELEVATOR_CONTACT)) && blockState.get(FACING) == direction.getOpposite();
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(POWERED);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        return side != null && state.get(FACING) != side.getOpposite();
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView blockAccess, BlockPos pos, Direction side) {
        return state.get(POWERED) && side != state.get(FACING).getOpposite() ? 15 : 0;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }
}
