package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

public class LinearChassisBlock extends AbstractChassisBlock {

    public static final BooleanProperty STICKY_TOP = BooleanProperty.create("sticky_top");
    public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.create("sticky_bottom");

    public LinearChassisBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STICKY_TOP, false).setValue(STICKY_BOTTOM, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(STICKY_TOP, STICKY_BOTTOM);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placedOnPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        BlockState blockState = context.getLevel().getBlockState(placedOnPos);

        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            if (isChassis(blockState))
                return defaultBlockState().setValue(AXIS, blockState.getValue(AXIS));
            return defaultBlockState().setValue(AXIS, context.getNearestLookingDirection().getAxis());
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader p_196271_4_,
        ScheduledTickAccess tickView,
        BlockPos p_196271_5_,
        Direction side,
        BlockPos p_196271_6_,
        BlockState other,
        RandomSource random
    ) {
        BooleanProperty property = getGlueableSide(state, side);
        if (property == null || !sameKind(state, other) || state.getValue(AXIS) != other.getValue(AXIS))
            return state;
        return state.setValue(property, false);
    }

    @Override
    @Nullable
    public BooleanProperty getGlueableSide(BlockState state, Direction face) {
        if (face.getAxis() != state.getValue(AXIS))
            return null;
        return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
    }

    @Override
    protected boolean glueAllowedOnSide(BlockGetter world, BlockPos pos, BlockState state, Direction side) {
        BlockState other = world.getBlockState(pos.relative(side));
        return !sameKind(other, state) || state.getValue(AXIS) != other.getValue(AXIS);
    }

    public static boolean isChassis(BlockState state) {
        return state.is(AllBlocks.LINEAR_CHASSIS) || state.is(AllBlocks.SECONDARY_LINEAR_CHASSIS);
    }

    public static boolean sameKind(BlockState state1, BlockState state2) {
        return state1.getBlock() == state2.getBlock();
    }
}
