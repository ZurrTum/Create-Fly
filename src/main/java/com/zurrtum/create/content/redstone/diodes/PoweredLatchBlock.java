package com.zurrtum.create.content.redstone.diodes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

public class PoweredLatchBlock extends ToggleLatchBlock {

    public static BooleanProperty POWERED_SIDE = BooleanProperty.of("powered_side");

    public PoweredLatchBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED_SIDE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED_SIDE));
    }

    @Override
    protected void updatePowered(World worldIn, BlockPos pos, BlockState state) {
        boolean back = state.get(POWERED);
        boolean shouldBack = hasPower(worldIn, pos, state);
        boolean side = state.get(POWERED_SIDE);
        boolean shouldSide = isPoweredOnSides(worldIn, pos, state);

        TickPriority tickpriority = TickPriority.HIGH;
        if (isTargetNotAligned(worldIn, pos, state))
            tickpriority = TickPriority.EXTREMELY_HIGH;
        else if (side || back)
            tickpriority = TickPriority.VERY_HIGH;

        if (worldIn.getBlockTickScheduler().isTicking(pos, this))
            return;
        if (back != shouldBack || side != shouldSide)
            worldIn.scheduleBlockTick(pos, this, getUpdateDelayInternal(state), tickpriority);
    }

    protected boolean isPoweredOnSides(World worldIn, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        Direction left = direction.rotateYClockwise();
        Direction right = direction.rotateYCounterclockwise();

        for (Direction d : new Direction[]{left, right}) {
            BlockPos blockpos = pos.offset(d);
            int i = worldIn.getEmittedRedstonePower(blockpos, d);
            if (i > 0)
                return true;
            BlockState blockstate = worldIn.getBlockState(blockpos);
            if (blockstate.getBlock() == Blocks.REDSTONE_WIRE && blockstate.get(RedstoneWireBlock.POWER) > 0)
                return true;
        }
        return false;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        boolean back = state.get(POWERED);
        boolean shouldBack = hasPower(worldIn, pos, state);
        boolean side = state.get(POWERED_SIDE);
        boolean shouldSide = isPoweredOnSides(worldIn, pos, state);
        BlockState stateIn = state;

        if (back != shouldBack) {
            state = state.with(POWERED, shouldBack);
            if (shouldBack)
                state = state.with(POWERING, true);
            else if (side)
                state = state.with(POWERING, false);
        }

        if (side != shouldSide) {
            state = state.with(POWERED_SIDE, shouldSide);
            if (shouldSide)
                state = state.with(POWERING, false);
            else if (back)
                state = state.with(POWERING, true);
        }

        if (state != stateIn)
            worldIn.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
    }

    @Override
    protected ActionResult activated(World worldIn, BlockPos pos, BlockState state) {
        if (state.get(POWERED) != state.get(POWERED_SIDE))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!worldIn.isClient()) {
            float f = !state.get(POWERING) ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
            worldIn.setBlockState(pos, state.cycle(POWERING), Block.NOTIFY_LISTENERS);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        if (side == null)
            return false;
        return side.getAxis().isHorizontal();
    }

}
