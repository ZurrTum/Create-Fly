package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class GantryCarriageBlock extends DirectionalAxisKineticBlock implements IBE<GantryCarriageBlockEntity>, NeighborUpdateListeningBlock {

    public GantryCarriageBlock(Settings properties) {
        super(properties);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockState shaft = world.getBlockState(pos.offset(direction.getOpposite()));
        return shaft.getBlock() == AllBlocks.GANTRY_SHAFT && shaft.get(GantryShaftBlock.FACING).getAxis() != direction.getAxis();
    }

    @Override
    public void prepare(BlockState stateIn, WorldAccess worldIn, BlockPos pos, int flags, int count) {
        super.prepare(stateIn, worldIn, pos, flags, count);
        withBlockEntityDo(worldIn, pos, GantryCarriageBlockEntity::checkValidGantryShaft);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    protected Direction getFacingForPlacement(ItemPlacementContext context) {
        return context.getSide();
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (!player.canModifyBlocks() || player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.isEmpty()) {
            withBlockEntityDo(level, pos, GantryCarriageBlockEntity::checkValidGantryShaft);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState stateForPlacement = super.getPlacementState(context);
        Direction opposite = stateForPlacement.get(FACING).getOpposite();
        return cycleAxisIfNecessary(stateForPlacement, opposite, context.getWorld().getBlockState(context.getBlockPos().offset(opposite)));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos updatePos, boolean isMoving) {
        if (updatePos.equals(pos.offset(state.get(FACING).getOpposite())) && !canPlaceAt(state, world, pos))
            world.breakBlock(pos, true);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState otherState,
        Random random
    ) {
        if (state.get(FACING) != direction.getOpposite())
            return state;
        return cycleAxisIfNecessary(state, direction, otherState);
    }

    protected BlockState cycleAxisIfNecessary(BlockState state, Direction direction, BlockState otherState) {
        if (otherState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return state;
        if (otherState.get(GantryShaftBlock.FACING).getAxis() == direction.getAxis())
            return state;
        if (isValidGantryShaftAxis(state, otherState))
            return state;
        return state.cycle(AXIS_ALONG_FIRST_COORDINATE);
    }

    public static boolean isValidGantryShaftAxis(BlockState pinionState, BlockState gantryState) {
        return getValidGantryShaftAxis(pinionState) == gantryState.get(GantryShaftBlock.FACING).getAxis();
    }

    public static Axis getValidGantryShaftAxis(BlockState state) {
        if (!(state.getBlock() instanceof GantryCarriageBlock block))
            return Axis.Y;
        Axis rotationAxis = block.getRotationAxis(state);
        Axis facingAxis = state.get(FACING).getAxis();
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
