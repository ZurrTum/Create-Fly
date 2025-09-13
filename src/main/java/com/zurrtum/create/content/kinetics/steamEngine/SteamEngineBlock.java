package com.zurrtum.create.content.kinetics.steamEngine;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SteamEngineBlock extends WallMountedBlock implements Waterloggable, IWrenchable, IBE<SteamEngineBlockEntity> {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public static final MapCodec<SteamEngineBlock> CODEC = createCodec(SteamEngineBlock::new);

    public SteamEngineBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(FACE, BlockFace.FLOOR).with(FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACE, FACING, Properties.WATERLOGGED));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return canAttach(pLevel, pPos, getConnectedDirection(pState).getOpposite());
    }

    public static boolean canAttach(WorldView pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.offset(pDirection);
        return pReader.getBlockState(blockpos).getBlock() instanceof FluidTankBlock;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
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
        if (placementHelper.matchesItem(stack))
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        FluidTankBlock.updateBoilerState(pState, pLevel, pPos.offset(getFacing(pState).getOpposite()));
        BlockPos shaftPos = getShaftPos(pState, pPos);
        BlockState shaftState = pLevel.getBlockState(shaftPos);
        if (isShaftValid(pState, shaftState))
            pLevel.setBlockState(shaftPos, PoweredShaftBlock.getEquivalent(shaftState), Block.NOTIFY_ALL);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        if (pState.hasBlockEntity())
            pLevel.removeBlockEntity(pPos);
        FluidTankBlock.updateBoilerState(pState, pLevel, pPos.offset(getFacing(pState).getOpposite()));
        BlockPos shaftPos = getShaftPos(pState, pPos);
        BlockState shaftState = pLevel.getBlockState(shaftPos);
        if (shaftState.isOf(AllBlocks.POWERED_SHAFT))
            pLevel.scheduleBlockTick(shaftPos, shaftState.getBlock(), 1);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        BlockFace face = pState.get(FACE);
        Direction direction = pState.get(FACING);
        return face == BlockFace.CEILING ? AllShapes.STEAM_ENGINE_CEILING.get(direction.getAxis()) : face == BlockFace.FLOOR ? AllShapes.STEAM_ENGINE.get(
            direction.getAxis()) : AllShapes.STEAM_ENGINE_WALL.get(direction);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        FluidState ifluidstate = level.getFluidState(pos);
        BlockState state = super.getPlacementState(context);
        if (state == null)
            return null;
        return state.with(Properties.WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    public static Direction getFacing(BlockState sideState) {
        return getConnectedDirection(sideState);
    }

    public static BlockPos getShaftPos(BlockState sideState, BlockPos pos) {
        return pos.offset(getConnectedDirection(sideState), 2);
    }

    public static boolean isShaftValid(BlockState state, BlockState shaft) {
        return (shaft.isOf(AllBlocks.SHAFT) || shaft.isOf(AllBlocks.POWERED_SHAFT)) && shaft.get(ShaftBlock.AXIS) != getFacing(state).getAxis();
    }

    @Override
    public Class<SteamEngineBlockEntity> getBlockEntityClass() {
        return SteamEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamEngineBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.STEAM_ENGINE;
    }

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.SHAFT);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof SteamEngineBlock;
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            BlockPos shaftPos = SteamEngineBlock.getShaftPos(state, pos);
            BlockState shaft = AllBlocks.SHAFT.getDefaultState();
            for (Direction direction : Direction.getEntityFacingOrder(player)) {
                shaft = shaft.with(ShaftBlock.AXIS, direction.getAxis());
                if (isShaftValid(state, shaft))
                    break;
            }

            BlockState newState = world.getBlockState(shaftPos);
            if (!newState.isReplaceable())
                return PlacementOffset.fail();

            Axis axis = shaft.get(ShaftBlock.AXIS);
            return PlacementOffset.success(
                shaftPos,
                s -> BlockHelper.copyProperties(s, (world.isClient ? AllBlocks.SHAFT : AllBlocks.POWERED_SHAFT).getDefaultState())
                    .with(PoweredShaftBlock.AXIS, axis)
            );
        }
    }

    public static Couple<Integer> getSpeedRange() {
        return Couple.create(16, 64);
    }

    public static Direction getConnectedDirection(BlockState state) {
        return WallMountedBlock.getDirection(state);
    }

    @Override
    protected @NotNull MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }

}
