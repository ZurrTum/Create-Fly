package com.zurrtum.create.content.decoration.girder;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.decoration.bracket.BracketBlock;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.chute.AbstractChuteBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.trains.display.FlapDisplayBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.block.WallMountedBlock.FACE;
import static net.minecraft.state.property.Properties.WATERLOGGED;

public class GirderBlock extends Block implements Waterloggable, IWrenchable {

    private static final int placementHelperId = PlacementHelpers.register(new GirderPlacementHelper());

    public static final BooleanProperty X = BooleanProperty.of("x");
    public static final BooleanProperty Z = BooleanProperty.of("z");
    public static final BooleanProperty TOP = BooleanProperty.of("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.of("bottom");
    public static final EnumProperty<Axis> AXIS = Properties.AXIS;

    public GirderBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(WATERLOGGED, false).with(AXIS, Axis.Y).with(TOP, false).with(BOTTOM, false).with(X, false)
            .with(Z, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(X, Z, TOP, BOTTOM, AXIS, WATERLOGGED));
    }

    @Override
    public VoxelShape getSidesShape(BlockState pState, BlockView pReader, BlockPos pPos) {
        return VoxelShapes.union(super.getSidesShape(pState, pReader, pPos), AllShapes.EIGHT_VOXEL_POLE.get(Axis.Y));
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
        if (player == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (stack.isOf(AllItems.SHAFT)) {
            KineticBlockEntity.switchToBlockState(
                level,
                pos,
                AllBlocks.METAL_GIRDER_ENCASED_SHAFT.getDefaultState().with(WATERLOGGED, state.get(WATERLOGGED)).with(TOP, state.get(TOP))
                    .with(BOTTOM, state.get(BOTTOM))
                    .with(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.get(X) || hitResult.getSide().getAxis() == Axis.Z ? Axis.Z : Axis.X)
            );

            level.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_HIT, SoundCategory.BLOCKS, 0.5f, 1.25f);
            if (!level.isClient() && !player.isCreative()) {
                stack.decrement(1);
                if (stack.isEmpty())
                    player.setStackInHand(hand, ItemStack.EMPTY);
            }

            return ActionResult.SUCCESS;
        }

        if (stack.isOf(AllItems.WRENCH) && !player.isSneaking()) {
            if (GirderWrenchBehavior.handleClick(level, pos, state, hitResult))
                return ActionResult.SUCCESS;
            return ActionResult.FAIL;
        }

        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public void scheduledTick(BlockState p_60462_, ServerWorld p_60463_, BlockPos p_60464_, Random p_60465_) {
        Block.replace(p_60462_, Block.postProcessState(p_60462_, p_60463_, p_60464_), p_60463_, p_60464_, 3);
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
        if (state.get(WATERLOGGED))
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        Axis axis = direction.getAxis();

        if (direction.getAxis() != Axis.Y) {
            if (state.get(AXIS) != direction.getAxis()) {
                Property<Boolean> updateProperty = axis == Axis.X ? X : axis == Axis.Z ? Z : direction == Direction.UP ? TOP : BOTTOM;
                if (!isConnected(world, pos, state, direction) && !isConnected(world, pos, state, direction.getOpposite()))
                    state = state.with(updateProperty, false);
            }
        } else if (state.get(AXIS) != Axis.Y) {
            if (world.getBlockState(pos.up()).getSidesShape(world, pos.up()).isEmpty())
                state = state.with(TOP, false);
            if (world.getBlockState(pos.down()).getSidesShape(world, pos.down()).isEmpty())
                state = state.with(BOTTOM, false);
        }

        for (Direction d : Iterate.directionsInAxis(axis))
            state = updateState(world, pos, state, d);

        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction face = context.getSide();
        FluidState ifluidstate = level.getFluidState(pos);
        BlockState state = super.getPlacementState(context);
        state = state.with(X, face.getAxis() == Axis.X);
        state = state.with(Z, face.getAxis() == Axis.Z);
        state = state.with(AXIS, face.getAxis());

        for (Direction d : Iterate.directions)
            state = updateState(level, pos, state, d);

        return state.with(WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
    }

    public static BlockState updateState(WorldView level, BlockPos pos, BlockState state, Direction d) {
        Axis axis = d.getAxis();
        Property<Boolean> updateProperty = axis == Axis.X ? X : axis == Axis.Z ? Z : d == Direction.UP ? TOP : BOTTOM;
        BlockState sideState = level.getBlockState(pos.offset(d));

        if (axis.isVertical())
            return updateVerticalProperty(level, pos, state, updateProperty, sideState, d);

        if (state.get(AXIS) == axis)
            state = state.with(updateProperty, true);
        else if (sideState.getBlock() instanceof GirderEncasedShaftBlock && sideState.get(GirderEncasedShaftBlock.HORIZONTAL_AXIS) != axis)
            state = state.with(updateProperty, true);
        else if (sideState.getBlock() == state.getBlock() && sideState.get(updateProperty))
            state = state.with(updateProperty, true);
        else if (sideState.getBlock() instanceof NixieTubeBlock && NixieTubeBlock.getFacing(sideState) == d)
            state = state.with(updateProperty, true);
        else if (sideState.getBlock() instanceof PlacardBlock && PlacardBlock.connectedDirection(sideState) == d)
            state = state.with(updateProperty, true);
        else if (isFacingBracket(level, pos, d))
            state = state.with(updateProperty, true);

        for (Direction d2 : Iterate.directionsInAxis(axis == Axis.X ? Axis.Z : Axis.X)) {
            BlockState above = level.getBlockState(pos.up().offset(d2));
            if (above.isIn(AllBlockTags.GIRDABLE_TRACKS)) {
                TrackShape shape = above.get(TrackBlock.SHAPE);
                if (shape == (axis == Axis.X ? TrackShape.XO : TrackShape.ZO))
                    state = state.with(updateProperty, true);
            }
        }

        return state;
    }

    public static boolean isFacingBracket(BlockRenderView level, BlockPos pos, Direction d) {
        BlockEntity blockEntity = level.getBlockEntity(pos.offset(d));
        if (!(blockEntity instanceof SmartBlockEntity sbe))
            return false;
        BracketedBlockEntityBehaviour behaviour = sbe.getBehaviour(BracketedBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return false;
        BlockState bracket = behaviour.getBracket();
        if (bracket == null || !bracket.contains(BracketBlock.FACING))
            return false;
        return bracket.get(BracketBlock.FACING) == d;
    }

    public static BlockState updateVerticalProperty(
        WorldView level,
        BlockPos pos,
        BlockState state,
        Property<Boolean> updateProperty,
        BlockState sideState,
        Direction d
    ) {
        boolean canAttach = false;

        if (state.contains(AXIS) && state.get(AXIS) == Axis.Y)
            canAttach = true;
        else if (isGirder(sideState) && isXGirder(sideState) == isZGirder(sideState))
            canAttach = true;
        else if (isGirder(sideState))
            canAttach = true;
        else if (sideState.contains(WallBlock.UP) && sideState.get(WallBlock.UP))
            canAttach = true;
        else if (sideState.getBlock() instanceof NixieTubeBlock && NixieTubeBlock.getFacing(sideState) == d)
            canAttach = true;
        else if (sideState.getBlock() instanceof FlapDisplayBlock)
            canAttach = true;
        else if (sideState.getBlock() instanceof LanternBlock && (d == Direction.DOWN) == (sideState.get(LanternBlock.HANGING)))
            canAttach = true;
        else if (sideState.getBlock() instanceof ChainBlock && sideState.get(ChainBlock.AXIS) == Axis.Y)
            canAttach = true;
        else if (sideState.contains(FACE)) {
            if (sideState.get(FACE) == BlockFace.CEILING && d == Direction.DOWN)
                canAttach = true;
            else if (sideState.get(FACE) == BlockFace.FLOOR && d == Direction.UP)
                canAttach = true;
        } else if (sideState.getBlock() instanceof PlacardBlock && PlacardBlock.connectedDirection(sideState) == d)
            canAttach = true;
        else if (isFacingBracket(level, pos, d))
            canAttach = true;

        if (canAttach)
            return state.with(updateProperty, true);
        return state;
    }

    public static boolean isGirder(BlockState state) {
        return state.getBlock() instanceof GirderBlock || state.getBlock() instanceof GirderEncasedShaftBlock;
    }

    public static boolean isXGirder(BlockState state) {
        return (state.getBlock() instanceof GirderBlock && state.get(X)) || (state.getBlock() instanceof GirderEncasedShaftBlock && state.get(
            GirderEncasedShaftBlock.HORIZONTAL_AXIS) == Axis.Z);
    }

    public static boolean isZGirder(BlockState state) {
        return (state.getBlock() instanceof GirderBlock && state.get(Z)) || (state.getBlock() instanceof GirderEncasedShaftBlock && state.get(
            GirderEncasedShaftBlock.HORIZONTAL_AXIS) == Axis.X);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        boolean x = state.get(GirderBlock.X);
        boolean z = state.get(GirderBlock.Z);
        return x ? z ? AllShapes.GIRDER_CROSS : AllShapes.GIRDER_BEAM.get(Axis.X) : z ? AllShapes.GIRDER_BEAM.get(Axis.Z) : AllShapes.EIGHT_VOXEL_POLE.get(
            Axis.Y);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    public static boolean isConnected(BlockRenderView world, BlockPos pos, BlockState state, Direction side) {
        Axis axis = side.getAxis();
        if (state.getBlock() instanceof GirderBlock && !state.get(axis == Axis.X ? X : Z))
            return false;
        if (state.getBlock() instanceof GirderEncasedShaftBlock && state.get(GirderEncasedShaftBlock.HORIZONTAL_AXIS) == axis)
            return false;
        BlockPos relative = pos.offset(side);
        BlockState blockState = world.getBlockState(relative);
        if (blockState.isAir())
            return false;
        if (blockState.getBlock() instanceof NixieTubeBlock && NixieTubeBlock.getFacing(blockState) == side)
            return true;
        if (isFacingBracket(world, pos, side))
            return true;
        //TODO
        //        if (blockState.getBlock() instanceof PlacardBlock && PlacardBlock.connectedDirection(blockState) == side)
        //            return true;
        VoxelShape shape = blockState.getOutlineShape(world, relative);
        if (shape.isEmpty())
            return false;
        if (Block.isFaceFullSquare(shape, side.getOpposite()) && blockState.isSolid())
            return true;
        return AbstractChuteBlock.getChuteFacing(blockState) == Direction.DOWN;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        state = state.with(AXIS, rot.rotate(Direction.from(state.get(AXIS), AxisDirection.POSITIVE)).getAxis());
        if (rot.rotate(Direction.EAST).getAxis() == Axis.X)
            return state;
        return state.with(X, state.get(Z)).with(Z, state.get(Z));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state;
    }

}
