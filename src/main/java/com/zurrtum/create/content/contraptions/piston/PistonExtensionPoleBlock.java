package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.*;

public class PistonExtensionPoleBlock extends WrenchableDirectionalBlock implements IWrenchable, SimpleWaterloggedBlock {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        Axis axis = state.getValue(FACING).getAxis();
        Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
        BlockPos pistonHead = null;
        BlockPos pistonBase = null;

        for (int modifier : new int[]{1, -1}) {
            for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset += modifier) {
                BlockPos currentPos = pos.relative(direction, offset);
                BlockState block = worldIn.getBlockState(currentPos);

                if (isExtensionPole(block) && axis == block.getValue(FACING).getAxis())
                    continue;

                if (isPiston(block) && block.getValue(BlockStateProperties.FACING).getAxis() == axis)
                    pistonBase = currentPos;

                if (isPistonHead(block) && block.getValue(BlockStateProperties.FACING).getAxis() == axis)
                    pistonHead = currentPos;

                break;
            }
        }

        if (pistonHead != null && pistonBase != null && worldIn.getBlockState(pistonHead)
            .getValue(BlockStateProperties.FACING) == worldIn.getBlockState(pistonBase).getValue(BlockStateProperties.FACING)) {

            final BlockPos basePos = pistonBase;
            BlockPos.betweenClosedStream(pistonBase, pistonHead).filter(p -> !p.equals(pos) && !p.equals(basePos))
                .forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
            worldIn.setBlockAndUpdate(basePos, worldIn.getBlockState(basePos).setValue(MechanicalPistonBlock.STATE, PistonState.RETRACTED));

            if (worldIn.getBlockEntity(basePos) instanceof MechanicalPistonBlockEntity baseBE) {
                baseBE.onLengthBroken();
            }
        }

        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(FACING).getAxis());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite())
            .setValue(BlockStateProperties.WATERLOGGED, FluidState.getType() == Fluids.WATER);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public static class PlacementHelper extends PoleHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper() {
            super(state -> state.is(AllBlocks.PISTON_EXTENSION_POLE), state -> state.getValue(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.is(AllItems.PISTON_EXTENSION_POLE);
        }
    }
}
