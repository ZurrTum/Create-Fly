package com.zurrtum.create.content.kinetics.waterwheel;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.equipment.goggles.IProxyHoveringInformation;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.LandingEffectControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

public class WaterWheelStructuralBlock extends FacingBlock implements IWrenchable, IProxyHoveringInformation, LandingEffectControlBlock {

    public static final MapCodec<WaterWheelStructuralBlock> CODEC = createCodec(WaterWheelStructuralBlock::new);

    public WaterWheelStructuralBlock(Settings p_52591_) {
        super(p_52591_);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.LARGE_WATER_WHEEL.getDefaultStack();
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        BlockPos clickedPos = context.getBlockPos();
        World level = context.getWorld();

        if (stillValid(level, clickedPos, state, false)) {
            BlockPos masterPos = getMaster(level, clickedPos, state);
            context = new ItemUsageContext(
                level,
                context.getPlayer(),
                context.getHand(),
                context.getStack(),
                new BlockHitResult(context.getHitPos(), context.getSide(), masterPos, context.hitsInsideBlock())
            );
            state = level.getBlockState(masterPos);
        }

        return IWrenchable.super.onSneakWrenched(state, context);
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
        if (!stillValid(level, pos, state, false))
            return ActionResult.FAIL;
        if (!(level.getBlockEntity(getMaster(level, pos, state)) instanceof WaterWheelBlockEntity wwt))
            return ActionResult.FAIL;
        return wwt.applyMaterialIfValid(stack);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        if (stillValid(pLevel, pPos, pState, false))
            pLevel.breakBlock(getMaster(pLevel, pPos, pState), true);
    }

    @Override
    public BlockState onBreak(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
        if (stillValid(pLevel, pPos, pState, false)) {
            BlockPos masterPos = getMaster(pLevel, pPos, pState);
            pLevel.setBlockBreakingInfo(masterPos.hashCode(), masterPos, -1);
            if (!pLevel.isClient() && pPlayer.isCreative())
                pLevel.breakBlock(masterPos, false);
        }
        return super.onBreak(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        Random random
    ) {
        if (stillValid(pLevel, pCurrentPos, pState, false)) {
            BlockPos masterPos = getMaster(pLevel, pCurrentPos, pState);
            if (!tickView.getBlockTickScheduler().isQueued(masterPos, AllBlocks.LARGE_WATER_WHEEL))
                tickView.scheduleBlockTick(masterPos, AllBlocks.LARGE_WATER_WHEEL, 1);
            return pState;
        }
        if (!(pLevel instanceof World level) || level.isClient())
            return pState;
        if (!level.getBlockTickScheduler().isQueued(pCurrentPos, this))
            level.scheduleBlockTick(pCurrentPos, this, 1);
        return pState;
    }

    public static BlockPos getMaster(BlockView level, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        BlockPos targetedPos = pos.offset(direction);
        BlockState targetedState = level.getBlockState(targetedPos);
        if (targetedState.isOf(AllBlocks.WATER_WHEEL_STRUCTURAL))
            return getMaster(level, targetedPos, targetedState);
        return targetedPos;
    }

    public boolean stillValid(BlockView level, BlockPos pos, BlockState state, boolean directlyAdjacent) {
        if (!state.isOf(this))
            return false;

        Direction direction = state.get(FACING);
        BlockPos targetedPos = pos.offset(direction);
        BlockState targetedState = level.getBlockState(targetedPos);

        if (!directlyAdjacent && stillValid(level, targetedPos, targetedState, true))
            return true;
        return targetedState.getBlock() instanceof LargeWaterWheelBlock && targetedState.get(LargeWaterWheelBlock.AXIS) != direction.getAxis();
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (!stillValid(pLevel, pPos, pState, false))
            pLevel.setBlockState(pPos, Blocks.AIR.getDefaultState());
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos, LivingEntity entity, double distance) {
        return true;
    }

    @Override
    public BlockPos getInformationSource(World level, BlockPos pos, BlockState state) {
        return stillValid(level, pos, state, false) ? getMaster(level, pos, state) : pos;
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}
