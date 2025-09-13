package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class DepotBlock extends Block implements IBE<DepotBlockEntity>, IWrenchable, ProperWaterloggedBlock, ItemInventoryProvider<DepotBlockEntity> {

    public DepotBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(WATERLOGGED));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, DepotBlockEntity blockEntity, Direction context) {
        return blockEntity.depotBehaviour.itemHandler;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
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
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return withWater(super.getPlacementState(pContext), pContext);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public Class<DepotBlockEntity> getBlockEntityClass() {
        return DepotBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DepotBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.DEPOT;
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
        return SharedDepotBlockMethods.onUse(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        SharedDepotBlockMethods.onLanded(worldIn, entityIn);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos) {
        return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
