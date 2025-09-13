package com.zurrtum.create.content.kinetics.waterwheel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class WaterWheelBlock extends DirectionalKineticBlock implements IBE<WaterWheelBlockEntity> {

    public WaterWheelBlock(Settings properties) {
        super(properties);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        for (Direction direction : Iterate.directions) {
            BlockPos neighbourPos = pos.offset(direction);
            BlockState neighbourState = worldIn.getBlockState(neighbourPos);
            if (!neighbourState.isOf(AllBlocks.WATER_WHEEL))
                continue;
            Axis axis = state.get(FACING).getAxis();
            if (neighbourState.get(FACING).getAxis() != axis || axis != direction.getAxis())
                return false;
        }
        return true;
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
        return onBlockEntityUseItemOn(level, pos, wwt -> wwt.applyMaterialIfValid(stack));
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState stateIn,
        WorldView worldIn,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction facing,
        BlockPos facingPos,
        BlockState facingState,
        Random random
    ) {
        if (worldIn instanceof WrappedLevel)
            return stateIn;
        if (worldIn.isClient())
            return stateIn;
        if (!tickView.getBlockTickScheduler().isQueued(currentPos, this))
            tickView.scheduleBlockTick(currentPos, this, 1);
        return stateIn;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        if (worldIn.isClient())
            return;
        if (!worldIn.getBlockTickScheduler().isQueued(pos, this))
            worldIn.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        withBlockEntityDo(pLevel, pPos, WaterWheelBlockEntity::determineAndApplyFlowScore);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        state.with(FACING, Direction.get(AxisDirection.POSITIVE, state.get(FACING).getAxis()));
        return state;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return state.get(FACING).getAxis() == face.getAxis();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public float getParticleTargetRadius() {
        return 1.125f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1f;
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    public Class<WaterWheelBlockEntity> getBlockEntityClass() {
        return WaterWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WaterWheelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.WATER_WHEEL;
    }
}
