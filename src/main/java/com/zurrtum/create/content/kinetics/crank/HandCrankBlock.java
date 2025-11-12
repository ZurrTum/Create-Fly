package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HandCrankBlock extends DirectionalKineticBlock implements IBE<HandCrankBlockEntity>, ProperWaterloggedBlock, NeighborUpdateListeningBlock {

    public HandCrankBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.CRANK.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED));
    }

    public int getRotationSpeed() {
        return 32;
    }

    public static boolean onBlockActivated(InteractionHand hand, BlockState state, ItemStack stack) {
        if (hand == InteractionHand.OFF_HAND || stack.is(AllItems.WRENCH)) {
            return false;
        }
        return state.getBlock() instanceof HandCrankBlock;
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
        if (player.isSpectator())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        withBlockEntityDo(level, pos, be -> be.turn(player.isShiftKeyDown()));
        if (!stack.is(AllItems.EXTENDO_GRIP))
            player.causeFoodExhaustion(getRotationSpeed() * AllConfigs.server().kinetics.crankHungerMultiplier.getF());

        if (player.getFoodData().getFoodLevel() == 0 && player instanceof ServerPlayer serverPlayer)
            AllAdvancements.HAND_CRANK.trigger(serverPlayer);

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        BlockState defaultBlockState = withWater(defaultBlockState(), context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()))
            return defaultBlockState.setValue(FACING, context.getClickedFace());
        return defaultBlockState.setValue(FACING, preferred.getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction facing = state.getValue(FACING).getOpposite();
        BlockPos neighbourPos = pos.relative(facing);
        BlockState neighbour = worldIn.getBlockState(neighbourPos);
        return !neighbour.getCollisionShape(worldIn, neighbourPos).isEmpty();
    }

    @Override
    public void neighborUpdate(BlockState state, Level worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClientSide())
            return;

        Direction blockFacing = state.getValue(FACING);
        if (fromPos.equals(pos.relative(blockFacing.getOpposite()))) {
            if (!canSurvive(state, worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<HandCrankBlockEntity> getBlockEntityClass() {
        return HandCrankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.HAND_CRANK;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
