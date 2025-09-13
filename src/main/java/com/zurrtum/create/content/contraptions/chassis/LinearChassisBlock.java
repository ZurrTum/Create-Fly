package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class LinearChassisBlock extends AbstractChassisBlock {

    public static final BooleanProperty STICKY_TOP = BooleanProperty.of("sticky_top");
    public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.of("sticky_bottom");

    public LinearChassisBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(STICKY_TOP, false).with(STICKY_BOTTOM, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(STICKY_TOP, STICKY_BOTTOM);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos placedOnPos = context.getBlockPos().offset(context.getSide().getOpposite());
        BlockState blockState = context.getWorld().getBlockState(placedOnPos);

        if (context.getPlayer() == null || !context.getPlayer().isSneaking()) {
            if (isChassis(blockState))
                return getDefaultState().with(AXIS, blockState.get(AXIS));
            return getDefaultState().with(AXIS, context.getPlayerLookDirection().getAxis());
        }
        return super.getPlacementState(context);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView p_196271_4_,
        ScheduledTickView tickView,
        BlockPos p_196271_5_,
        Direction side,
        BlockPos p_196271_6_,
        BlockState other,
        Random random
    ) {
        BooleanProperty property = getGlueableSide(state, side);
        if (property == null || !sameKind(state, other) || state.get(AXIS) != other.get(AXIS))
            return state;
        return state.with(property, false);
    }

    @Override
    public BooleanProperty getGlueableSide(BlockState state, Direction face) {
        if (face.getAxis() != state.get(AXIS))
            return null;
        return face.getDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
    }

    @Override
    protected boolean glueAllowedOnSide(BlockView world, BlockPos pos, BlockState state, Direction side) {
        BlockState other = world.getBlockState(pos.offset(side));
        return !sameKind(other, state) || state.get(AXIS) != other.get(AXIS);
    }

    public static boolean isChassis(BlockState state) {
        return state.isOf(AllBlocks.LINEAR_CHASSIS) || state.isOf(AllBlocks.SECONDARY_LINEAR_CHASSIS);
    }

    public static boolean sameKind(BlockState state1, BlockState state2) {
        return state1.getBlock() == state2.getBlock();
    }
}
