package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.PistonState;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isExtensionPole;

public class MechanicalPistonHeadBlock extends WrenchableDirectionalBlock implements Waterloggable {

    public static final EnumProperty<PistonType> TYPE = Properties.PISTON_TYPE;

    public MechanicalPistonHeadBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(TYPE, Properties.WATERLOGGED);
        super.appendProperties(builder);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.PISTON_EXTENSION_POLE.getDefaultStack();
    }

    @Override
    public BlockState onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction direction = state.get(FACING);
        BlockPos pistonHead = pos;
        BlockPos pistonBase = null;

        for (int offset = 1; offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset++) {
            BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
            BlockState block = worldIn.getBlockState(currentPos);

            if (isExtensionPole(block) && direction.getAxis() == block.get(Properties.FACING).getAxis())
                continue;

            if (MechanicalPistonBlock.isPiston(block) && block.get(Properties.FACING) == direction)
                pistonBase = currentPos;

            break;
        }

        if (pistonHead != null && pistonBase != null) {
            final BlockPos basePos = pistonBase;
            BlockPos.stream(pistonBase, pistonHead).filter(p -> !p.equals(pos) && !p.equals(basePos))
                .forEach(p -> worldIn.breakBlock(p, !player.isCreative()));
            worldIn.setBlockState(basePos, worldIn.getBlockState(basePos).with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
        }

        return super.onBreak(worldIn, pos, state, player);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.MECHANICAL_PISTON_HEAD.get(state.get(FACING));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        Random random
    ) {
        if (state.get(Properties.WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState FluidState = context.getWorld().getFluidState(context.getBlockPos());
        return super.getPlacementState(context).with(Properties.WATERLOGGED, Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }
}
