package com.zurrtum.create.content.trains.display;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class FlapDisplayBlock extends HorizontalKineticBlock implements IBE<FlapDisplayBlockEntity>, IWrenchable, ICogWheel, Waterloggable {

    public static final BooleanProperty UP = BooleanProperty.of("up");
    public static final BooleanProperty DOWN = BooleanProperty.of("down");

    public FlapDisplayBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(UP, false).with(DOWN, false).with(WATERLOGGED, false));
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return super.areStatesKineticallyEquivalent(oldState, newState);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).getAxis();
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(UP, DOWN, WATERLOGGED));
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction face = context.getSide();
        BlockPos clickedPos = context.getBlockPos();
        BlockPos placedOnPos = clickedPos.offset(face.getOpposite());
        World level = context.getWorld();
        BlockState blockState = level.getBlockState(placedOnPos);
        BlockState stateForPlacement = getDefaultState();
        FluidState ifluidstate = context.getWorld().getFluidState(context.getBlockPos());

        if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer().isSneaking()))
            stateForPlacement = super.getPlacementState(context);
        else {
            Direction otherFacing = blockState.get(HORIZONTAL_FACING);
            stateForPlacement = stateForPlacement.with(HORIZONTAL_FACING, otherFacing);
        }

        return updateColumn(level, clickedPos, stateForPlacement.with(WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER), true);
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
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack))
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        FlapDisplayBlockEntity flapBE = getBlockEntity(level, pos);

        if (flapBE == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        flapBE = flapBE.getController();
        if (flapBE == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        double yCoord = hitResult.getPos().add(Vec3d.of(hitResult.getSide().getOpposite().getVector()).multiply(.125f)).y;

        int lineIndex = flapBE.getLineIndexAt(yCoord);

        if (stack.isEmpty()) {
            if (!flapBE.isSpeedRequirementFulfilled())
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            flapBE.applyTextManually(lineIndex, null);
            return ActionResult.SUCCESS;
        }

        if (stack.getItem() == Items.GLOW_INK_SAC) {
            if (!level.isClient()) {
                level.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                flapBE.setGlowing(lineIndex);
            }
            return ActionResult.SUCCESS;
        }

        boolean display = stack.getItem() == Items.NAME_TAG && stack.contains(DataComponentTypes.CUSTOM_NAME) || stack.isOf(AllItems.CLIPBOARD);
        DyeColor dye = AllItemTags.getDyeColor(stack);

        if (!display && dye == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (dye == null && !flapBE.isSpeedRequirementFulfilled())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient())
            return ActionResult.SUCCESS;

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);

        if (display) {
            if (stack.isOf(AllItems.CLIPBOARD)) {
                List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
                int line = lineIndex;
                for (ClipboardEntry entry : entries) {
                    for (String string : entry.text.getString().split("\n")) {
                        flapBE.applyTextManually(line++, Text.literal(string));
                    }
                }
                return ActionResult.SUCCESS;
            }

            flapBE.applyTextManually(lineIndex, customName);
        }
        if (dye != null) {
            level.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            flapBE.setColour(lineIndex, dye);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.FLAP_DISPLAY.get(pState.get(HORIZONTAL_FACING));
    }

    @Override
    public Class<FlapDisplayBlockEntity> getBlockEntityClass() {
        return FlapDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FlapDisplayBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.FLAP_DISPLAY;
    }

    @Override
    public float getParticleTargetRadius() {
        return .85f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .75f;
    }

    private BlockState updateColumn(World level, BlockPos pos, BlockState state, boolean present) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        Axis axis = getConnectionAxis(state);

        for (Direction connection : Iterate.directionsInAxis(Axis.Y)) {
            boolean connect = true;

            Move:
            for (Direction movement : Iterate.directionsInAxis(axis)) {
                currentPos.set(pos);
                for (int i = 0; i < 1000; i++) {
                    if (!level.isPosLoaded(currentPos))
                        break;

                    BlockState other1 = currentPos.equals(pos) ? state : level.getBlockState(currentPos);
                    BlockState other2 = level.getBlockState(currentPos.offset(connection));
                    boolean col1 = canConnect(state, other1);
                    boolean col2 = canConnect(state, other2);
                    currentPos.move(movement);

                    if (!col1 && !col2)
                        break;
                    if (col1 && col2)
                        continue;

                    connect = false;
                    break Move;
                }
            }
            state = setConnection(state, connection, connect);
        }
        return state;
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onBlockAdded(pState, pLevel, pPos, pOldState, pIsMoving);
        if (pOldState.getBlock() == this)
            return;
        QueryableTickScheduler<Block> blockTicks = pLevel.getBlockTickScheduler();
        if (!blockTicks.isQueued(pPos, this))
            pLevel.scheduleBlockTick(pPos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        if (pState.getBlock() != this)
            return;
        BlockPos belowPos = pPos.offset(Direction.from(getConnectionAxis(pState), AxisDirection.NEGATIVE));
        BlockState belowState = pLevel.getBlockState(belowPos);
        if (!canConnect(pState, belowState))
            KineticBlockEntity.switchToBlockState(pLevel, pPos, updateColumn(pLevel, pPos, pState, true));
        withBlockEntityDo(pLevel, pPos, FlapDisplayBlockEntity::updateControllerStatus);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        return updatedShapeInner(state, pDirection, pNeighborState, pLevel, tickView, pCurrentPos);
    }

    private BlockState updatedShapeInner(
        BlockState state,
        Direction pDirection,
        BlockState pNeighborState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos
    ) {
        if (state.get(WATERLOGGED))
            tickView.scheduleFluidTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickRate(pLevel));
        if (!canConnect(state, pNeighborState))
            return setConnection(state, pDirection, false);
        if (pDirection.getAxis() == getConnectionAxis(state))
            return getStateWithProperties(pNeighborState).with(WATERLOGGED, state.get(WATERLOGGED));
        return setConnection(state, pDirection, getConnection(pNeighborState, pDirection.getOpposite()));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    protected boolean canConnect(BlockState state, BlockState other) {
        return other.getBlock() == this && state.get(HORIZONTAL_FACING) == other.get(HORIZONTAL_FACING);
    }

    protected Axis getConnectionAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    public static boolean getConnection(BlockState state, Direction side) {
        BooleanProperty property = side == Direction.DOWN ? DOWN : side == Direction.UP ? UP : null;
        return property != null && state.get(property);
    }

    public static BlockState setConnection(BlockState state, Direction side, boolean connect) {
        BooleanProperty property = side == Direction.DOWN ? DOWN : side == Direction.UP ? UP : null;
        if (property != null)
            state = state.with(property, connect);
        return state;
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        super.onStateReplaced(pState, pLevel, pPos, pIsMoving);
        if (pIsMoving)
            return;
        for (Direction d : Iterate.directionsInAxis(getConnectionAxis(pState))) {
            BlockPos relative = pPos.offset(d);
            BlockState adjacent = pLevel.getBlockState(relative);
            if (canConnect(pState, adjacent))
                KineticBlockEntity.switchToBlockState(pLevel, relative, updateColumn(pLevel, relative, adjacent, false));
        }
    }

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.DISPLAY_BOARD);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.isOf(AllBlocks.DISPLAY_BOARD);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                state.get(FlapDisplayBlock.HORIZONTAL_FACING).getAxis(),
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            return directions.isEmpty() ? PlacementOffset.fail() : PlacementOffset.success(
                pos.offset(directions.getFirst()),
                s -> AllBlocks.DISPLAY_BOARD.updateColumn(
                    world,
                    pos.offset(directions.getFirst()),
                    s.with(HORIZONTAL_FACING, state.get(FlapDisplayBlock.HORIZONTAL_FACING)),
                    true
                )
            );
        }
    }

}
