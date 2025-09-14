package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.function.Predicate;

import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.*;

public class PistonExtensionPoleBlock extends WrenchableDirectionalBlock implements IWrenchable, Waterloggable {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(Properties.WATERLOGGED, false));
    }

    @Override
    public BlockState onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        Axis axis = state.get(FACING).getAxis();
        Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
        BlockPos pistonHead = null;
        BlockPos pistonBase = null;

        for (int modifier : new int[]{1, -1}) {
            for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset += modifier) {
                BlockPos currentPos = pos.offset(direction, offset);
                BlockState block = worldIn.getBlockState(currentPos);

                if (isExtensionPole(block) && axis == block.get(FACING).getAxis())
                    continue;

                if (isPiston(block) && block.get(Properties.FACING).getAxis() == axis)
                    pistonBase = currentPos;

                if (isPistonHead(block) && block.get(Properties.FACING).getAxis() == axis)
                    pistonHead = currentPos;

                break;
            }
        }

        if (pistonHead != null && pistonBase != null && worldIn.getBlockState(pistonHead).get(Properties.FACING) == worldIn.getBlockState(pistonBase)
            .get(Properties.FACING)) {

            final BlockPos basePos = pistonBase;
            BlockPos.stream(pistonBase, pistonHead).filter(p -> !p.equals(pos) && !p.equals(basePos))
                .forEach(p -> worldIn.breakBlock(p, !player.isCreative()));
            worldIn.setBlockState(basePos, worldIn.getBlockState(basePos).with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));

            if (worldIn.getBlockEntity(basePos) instanceof MechanicalPistonBlockEntity baseBE) {
                baseBE.onLengthBroken();
            }
        }

        return super.onBreak(worldIn, pos, state, player);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.FOUR_VOXEL_POLE.get(state.get(FACING).getAxis());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState FluidState = context.getWorld().getFluidState(context.getBlockPos());
        return getDefaultState().with(FACING, context.getSide().getOpposite())
            .with(Properties.WATERLOGGED, Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
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
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack) && !player.isSneaking())
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED);
        super.appendProperties(builder);
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    public static class PlacementHelper extends PoleHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper() {
            super(state -> state.isOf(AllBlocks.PISTON_EXTENSION_POLE), state -> state.get(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.PISTON_EXTENSION_POLE);
        }
    }
}
