package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DrillBlock extends DirectionalKineticBlock implements IBE<DrillBlockEntity>, Waterloggable {
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public DrillBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, EntityCollisionHandler handler, boolean bl) {
        if (entityIn instanceof ItemEntity)
            return;
        if (!new Box(pos).contract(.1f).intersects(entityIn.getBoundingBox()))
            return;
        withBlockEntityDo(
            worldIn, pos, be -> {
                if (be.getSpeed() == 0)
                    return;
                if (worldIn instanceof ServerWorld serverWorld) {
                    entityIn.damage(serverWorld, AllDamageSources.get(worldIn).drill, (float) getDamage(be.getSpeed()));
                }
            }
        );
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.CASING_12PX.get(state.get(FACING));
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        withBlockEntityDo(worldIn, pos, DrillBlockEntity::destroyNextTick);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == state.get(FACING).getOpposite();
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState FluidState = context.getWorld().getFluidState(context.getBlockPos());
        return super.getPlacementState(context).with(Properties.WATERLOGGED, FluidState.getFluid() == Fluids.WATER);
    }

    public static double getDamage(float speed) {
        float speedAbs = Math.abs(speed);
        double sub1 = Math.min(speedAbs / 16, 2);
        double sub2 = Math.min(speedAbs / 32, 4);
        double sub3 = Math.min(speedAbs / 64, 4);
        return MathHelper.clamp(sub1 + sub2 + sub3, 1, 10);
    }

    @Override
    public Class<DrillBlockEntity> getBlockEntityClass() {
        return DrillBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DrillBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.DRILL;
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
        if (!player.isSneaking() && player.canModifyBlocks()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.MECHANICAL_DRILL);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.isOf(AllBlocks.MECHANICAL_DRILL);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                state.get(FACING).getAxis(),
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(pos.offset(directions.getFirst()), s -> s.with(FACING, state.get(FACING)));
            }
        }
    }

}