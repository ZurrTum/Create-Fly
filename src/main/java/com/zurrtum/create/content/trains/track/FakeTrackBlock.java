package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.block.LandingEffectControlBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.block.RunningEffectControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class FakeTrackBlock extends Block implements BlockEntityProvider, ProperWaterloggedBlock, LandingEffectControlBlock, RunningEffectControlBlock {

    public FakeTrackBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return VoxelShapes.empty();
    }

    //TODO
    //    @Override
    //    public @Nullable PathType getBlockPathType(BlockState state, BlockView level, BlockPos pos, @Nullable Mob mob) {
    //        return PathType.DAMAGE_OTHER;
    //    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(WATERLOGGED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return withWater(super.getPlacementState(pContext), pContext);
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
    public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (pLevel.getBlockEntity(pPos) instanceof FakeTrackBlockEntity be)
            be.randomTick();
    }

    public static void keepAlive(WorldAccess level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FakeTrackBlockEntity be)
            be.keepAlive();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return AllBlockEntityTypes.FAKE_TRACK.instantiate(pPos, pState);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos, LivingEntity entity, double distance) {
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, World level, BlockPos pos, Entity entity) {
        return true;
    }

}
