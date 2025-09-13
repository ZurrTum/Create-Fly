package com.zurrtum.create.content.decoration;

import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class TrainTrapdoorBlock extends TrapdoorBlock implements IWrenchable {
    public TrainTrapdoorBlock(BlockSetType type, Settings properties) {
        super(type, properties);
    }

    public static TrainTrapdoorBlock metal(Settings properties) {
        return new TrainTrapdoorBlock(SlidingDoorBlock.TRAIN_SET_TYPE.get(), properties);
    }

    public static TrainTrapdoorBlock glass(Settings properties) {
        return new TrainTrapdoorBlock(SlidingDoorBlock.GLASS_SET_TYPE.get(), properties);
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        state = state.cycle(OPEN);
        level.setBlockState(pos, state, NOTIFY_LISTENERS);
        if (state.get(WATERLOGGED))
            level.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(level));
        playToggleSound(player, level, pos, state.get(OPEN));
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState other, Direction pDirection) {
        return state.isOf(this) == other.isOf(this) && isConnected(state, other, pDirection);
    }

    public static boolean isConnected(BlockState state, BlockState other, Direction pDirection) {
        state = state.with(WATERLOGGED, false).with(POWERED, false);
        other = other.with(WATERLOGGED, false).with(POWERED, false);

        boolean open = state.get(OPEN);
        BlockHalf half = state.get(HALF);
        Direction facing = state.get(FACING);

        if (open != other.get(OPEN))
            return false;
        if (!open && half == other.get(HALF))
            return pDirection.getAxis() != Axis.Y;
        if (!open && half != other.get(HALF) && pDirection.getAxis() == Axis.Y)
            return true;
        if (open && facing.getOpposite() == other.get(FACING) && pDirection.getAxis() == facing.getAxis())
            return true;
        if ((open ? state.with(HALF, BlockHalf.TOP) : state) != (open ? other.with(HALF, BlockHalf.TOP) : other))
            return false;

        return pDirection.getAxis() != facing.getAxis();
    }
}