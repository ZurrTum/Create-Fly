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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class HandCrankBlock extends DirectionalKineticBlock implements IBE<HandCrankBlockEntity>, ProperWaterloggedBlock, NeighborUpdateListeningBlock {

    public HandCrankBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.CRANK.get(state.get(FACING));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(WATERLOGGED));
    }

    public int getRotationSpeed() {
        return 32;
    }

    public static boolean onBlockActivated(Hand hand, BlockState state, ItemStack stack) {
        if (hand == Hand.OFF_HAND || stack.isOf(AllItems.WRENCH)) {
            return false;
        }
        return state.getBlock() instanceof HandCrankBlock;
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
        if (player.isSpectator())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        withBlockEntityDo(level, pos, be -> be.turn(player.isSneaking()));
        if (!stack.isOf(AllItems.EXTENDO_GRIP))
            player.addExhaustion(getRotationSpeed() * AllConfigs.server().kinetics.crankHungerMultiplier.getF());

        if (player.getHungerManager().getFoodLevel() == 0 && player instanceof ServerPlayerEntity serverPlayer)
            AllAdvancements.HAND_CRANK.trigger(serverPlayer);

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredFacing(context);
        BlockState defaultBlockState = withWater(getDefaultState(), context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isSneaking()))
            return defaultBlockState.with(FACING, context.getSide());
        return defaultBlockState.with(FACING, preferred.getOpposite());
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        Direction facing = state.get(FACING).getOpposite();
        BlockPos neighbourPos = pos.offset(facing);
        BlockState neighbour = worldIn.getBlockState(neighbourPos);
        return !neighbour.getCollisionShape(worldIn, neighbourPos).isEmpty();
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClient)
            return;

        Direction blockFacing = state.get(FACING);
        if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
            if (!canPlaceAt(state, worldIn, pos)) {
                worldIn.breakBlock(pos, true);
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(FACING).getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }
}
