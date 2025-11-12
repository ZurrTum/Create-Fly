package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GantryCarriageBlock extends DirectionalAxisKineticBlock implements IBE<GantryCarriageBlockEntity>, NeighborUpdateListeningBlock {

    public GantryCarriageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockState shaft = world.getBlockState(pos.relative(direction.getOpposite()));
        return shaft.getBlock() == AllBlocks.GANTRY_SHAFT && shaft.getValue(GantryShaftBlock.FACING).getAxis() != direction.getAxis();
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags, int count) {
        super.updateIndirectNeighbourShapes(stateIn, worldIn, pos, flags, count);
        withBlockEntityDo(worldIn, pos, GantryCarriageBlockEntity::checkValidGantryShaft);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        return context.getClickedFace();
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!player.mayBuild() || player.isShiftKeyDown())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.isEmpty()) {
            withBlockEntityDo(level, pos, GantryCarriageBlockEntity::checkValidGantryShaft);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState stateForPlacement = super.getStateForPlacement(context);
        Direction opposite = stateForPlacement.getValue(FACING).getOpposite();
        return cycleAxisIfNecessary(stateForPlacement, opposite, context.getLevel().getBlockState(context.getClickedPos().relative(opposite)));
    }

    @Override
    public void neighborUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos updatePos, boolean isMoving) {
        if (updatePos.equals(pos.relative(state.getValue(FACING).getOpposite())) && !canSurvive(state, world, pos))
            world.destroyBlock(pos, true);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState otherState,
        RandomSource random
    ) {
        if (state.getValue(FACING) != direction.getOpposite())
            return state;
        return cycleAxisIfNecessary(state, direction, otherState);
    }

    protected BlockState cycleAxisIfNecessary(BlockState state, Direction direction, BlockState otherState) {
        if (otherState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return state;
        if (otherState.getValue(GantryShaftBlock.FACING).getAxis() == direction.getAxis())
            return state;
        if (isValidGantryShaftAxis(state, otherState))
            return state;
        return state.cycle(AXIS_ALONG_FIRST_COORDINATE);
    }

    public static boolean isValidGantryShaftAxis(BlockState pinionState, BlockState gantryState) {
        return getValidGantryShaftAxis(pinionState) == gantryState.getValue(GantryShaftBlock.FACING).getAxis();
    }

    public static Axis getValidGantryShaftAxis(BlockState state) {
        if (!(state.getBlock() instanceof GantryCarriageBlock block))
            return Axis.Y;
        Axis rotationAxis = block.getRotationAxis(state);
        Axis facingAxis = state.getValue(FACING).getAxis();
        for (Axis axis : Iterate.axes)
            if (axis != rotationAxis && axis != facingAxis)
                return axis;
        return Axis.Y;
    }

    @Override
    public Class<GantryCarriageBlockEntity> getBlockEntityClass() {
        return GantryCarriageBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GantryCarriageBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.GANTRY_PINION;
    }

}
