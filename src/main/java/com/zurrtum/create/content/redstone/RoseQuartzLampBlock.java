package com.zurrtum.create.content.redstone;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class RoseQuartzLampBlock extends Block implements IWrenchable, WeakPowerControlBlock {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty POWERING = BrassDiodeBlock.POWERING;
    public static final BooleanProperty ACTIVATE = BooleanProperty.of("activate");

    public RoseQuartzLampBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(POWERED, false).with(POWERING, false).with(ACTIVATE, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        return stateForPlacement.with(POWERED, pContext.getWorld().isReceivingRedstonePower(pContext.getBlockPos()));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(POWERED, POWERING, ACTIVATE));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pIsMoving
    ) {
        if (pLevel.isClient())
            return;

        boolean isPowered = pState.get(POWERED);
        if (isPowered == pLevel.isReceivingRedstonePower(pPos))
            return;
        if (isPowered) {
            pLevel.setBlockState(pPos, pState.cycle(POWERED), Block.NOTIFY_LISTENERS);
            return;
        }

        forEachInCluster(
            pLevel, pPos, (currentPos, currentState) -> {
                pLevel.setBlockState(currentPos, currentState.with(POWERING, false), Block.NOTIFY_LISTENERS);
                scheduleActivation(pLevel, currentPos);
            }
        );

        pLevel.setBlockState(pPos, pState.with(POWERED, true).with(POWERING, true).with(ACTIVATE, true), Block.NOTIFY_LISTENERS);
        pLevel.updateNeighborsAlways(pPos, this, null);
        scheduleActivation(pLevel, pPos);
    }

    private void scheduleActivation(World pLevel, BlockPos pPos) {
        if (!pLevel.getBlockTickScheduler().isQueued(pPos, this))
            pLevel.scheduleBlockTick(pPos, this, 1);
    }

    private void forEachInCluster(World pLevel, BlockPos pPos, BiConsumer<BlockPos, BlockState> callback) {
        List<BlockPos> frontier = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(pPos);
        visited.add(pPos);

        while (!frontier.isEmpty()) {
            BlockPos pos = frontier.removeFirst();
            for (Direction d : Iterate.directions) {
                BlockPos currentPos = pos.offset(d);
                if (currentPos.getManhattanDistance(pPos) > 16)
                    continue;
                if (!visited.add(currentPos))
                    continue;
                BlockState currentState = pLevel.getBlockState(currentPos);
                if (!currentState.isOf(this))
                    continue;
                callback.accept(currentPos, currentState);
                frontier.add(currentPos);
            }
        }
    }

    @Override
    public boolean emitsRedstonePower(BlockState pState) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState pState, BlockView pLevel, BlockPos pPos, Direction pDirection) {
        if (pDirection == null)
            return 0;
        BlockState toState = pLevel.getBlockState(pPos.offset(pDirection.getOpposite()));
        if (toState.isOf(this))
            return 0;
        if (toState.isOf(Blocks.COMPARATOR))
            return getDistanceToPowered(pLevel, pPos, pDirection);
        //		if (toState.is(Blocks.REDSTONE_WIRE))
        //			return 0;
        return pState.get(POWERING) ? 15 : 0;
    }

    private int getDistanceToPowered(BlockView level, BlockPos pos, Direction column) {
        BlockPos.Mutable currentPos = pos.mutableCopy();
        for (int power = 15; power > 0; power--) {
            BlockState blockState = level.getBlockState(currentPos);
            if (!blockState.isOf(this))
                return 0;
            if (blockState.get(POWERING))
                return power;
            currentPos.move(column);
        }
        return 0;
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
        boolean wasPowering = pState.get(POWERING);
        boolean shouldBePowering = pState.get(ACTIVATE);

        if (wasPowering || shouldBePowering) {
            pLevel.setBlockState(pPos, pState.with(ACTIVATE, false).with(POWERING, shouldBePowering), Block.NOTIFY_LISTENERS);
        }

        pLevel.updateNeighborsAlways(pPos, this, null);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(POWERING);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult onWrenched = IWrenchable.super.onWrenched(state, context);
        if (!onWrenched.isAccepted())
            return onWrenched;

        forEachInCluster(
            context.getWorld(),
            context.getBlockPos(),
            (currentPos, currentState) -> context.getWorld().updateNeighborsAlways(currentPos, this, null)
        );
        return onWrenched;
    }

}
