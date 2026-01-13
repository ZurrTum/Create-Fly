package com.zurrtum.create.content.redstone.nixieTube;

import com.zurrtum.create.*;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class NixieTubeBlock extends DoubleFaceAttachedBlock implements IBE<NixieTubeBlockEntity>, IWrenchable, Waterloggable, SpecialBlockItemRequirement, RedStoneConnectBlock {
    protected final DyeColor color;

    public NixieTubeBlock(Settings properties, DyeColor color) {
        super(properties);
        this.color = color;
        setDefaultState(getDefaultState().with(FACE, DoubleAttachFace.FLOOR).with(WATERLOGGED, false));
    }

    public NixieTubeBlock(Settings properties) {
        this(properties, DyeColor.ORANGE);
    }

    public static Function<Settings, NixieTubeBlock> dyed(DyeColor color) {
        return properties -> new NixieTubeBlock(properties, color);
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

        NixieTubeBlockEntity nixie = getBlockEntity(level, pos);

        if (nixie == null) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        // Refuse interaction if nixie tube is in a computer-controlled row
        if (isInComputerControlledRow(level, pos)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (stack.isEmpty()) {
            if (nixie.reactsToRedstone())
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            nixie.clearCustomText();
            updateDisplayedRedstoneValue(state, level, pos);
            return ActionResult.SUCCESS;
        }

        boolean display = stack.getItem() == Items.NAME_TAG && stack.contains(DataComponentTypes.CUSTOM_NAME) || stack.isOf(AllItems.CLIPBOARD);
        DyeColor dye = AllItemTags.getDyeColor(stack);

        if (!display && dye == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        Text component;

        if (stack.isOf(AllItems.CLIPBOARD)) {
            List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
            component = entries.isEmpty() ? stack.getOrDefault(DataComponentTypes.CUSTOM_NAME, ScreenTexts.EMPTY) : entries.getFirst().text;
        } else {
            component = stack.getOrDefault(DataComponentTypes.CUSTOM_NAME, ScreenTexts.EMPTY);
        }

        if (level.isClient())
            return ActionResult.SUCCESS;

        // Skip computer check in this walk since it was already performed at the start.
        walkNixies(
            level, pos, true, (currentPos, rowPosition) -> {
                if (display)
                    withBlockEntityDo(level, currentPos, be -> be.displayCustomText(component, rowPosition));
                if (dye != null)
                    level.setBlockState(currentPos, withColor(state, dye));
            }
        );

        return ActionResult.SUCCESS;
    }

    public static Direction getLeftNixieDirection(@NotNull BlockState state) {
        Direction left = state.get(FACING).getOpposite();

        if (state.get(FACE) == DoubleAttachFace.WALL)
            left = Direction.UP;
        if (state.get(FACE) == DoubleAttachFace.WALL_REVERSED)
            left = Direction.DOWN;
        return left;
    }

    public static Direction getRightNixieDirection(@NotNull BlockState state) {
        return getLeftNixieDirection(state).getOpposite();
    }

    public static boolean isInComputerControlledRow(@NotNull WorldAccess world, @NotNull BlockPos pos) {
        return Mods.COMPUTERCRAFT.isLoaded() && !walkNixies(world, pos, false, null);
    }

    /**
     * Walk down a nixie tube row and execute a callback on each tube in said row.
     *
     * @param world                   The world the tubes are in.
     * @param start                   Start position for the walk.
     * @param allowComputerControlled Allow or disallow running callbacks if the row is computer-controlled.
     * @param callback                Callback to run for each tube.
     * @return True if the row was walked, false if the walk was aborted because it is computer-controlled.
     */
    public static boolean walkNixies(
        @NotNull WorldAccess world,
        @NotNull BlockPos start,
        boolean allowComputerControlled,
        @Nullable BiConsumer<BlockPos, Integer> callback
    ) {
        BlockState state = world.getBlockState(start);
        if (!(state.getBlock() instanceof NixieTubeBlock))
            return false;

        // If ComputerCraft is not installed, ignore allowComputerControlled since
        // nixies can't be computer-controlled
        if (!Mods.COMPUTERCRAFT.isLoaded())
            allowComputerControlled = true;

        BlockPos currentPos = start;
        Direction left = getLeftNixieDirection(state);
        Direction right = left.getOpposite();

        while (true) {
            BlockPos nextPos = currentPos.offset(left);
            if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
                break;
            // If computer-controlled nixie walking is disallowed, presence of any (same-color)
            // controlled nixies aborts the entire nixie walk.
            if (!allowComputerControlled && world.getBlockEntity(nextPos) instanceof NixieTubeBlockEntity ntbe && AbstractComputerBehaviour.contains(
                ntbe)) {
                return false;
            }
            currentPos = nextPos;
        }

        // As explained above, a controlled nixie in the row aborts the walk if they are disallowed,
        // and that includes those down the chain too.
        if (!allowComputerControlled) {
            // Check the start block itself
            if (world.getBlockEntity(start) instanceof NixieTubeBlockEntity ntbe && AbstractComputerBehaviour.contains(ntbe)) {
                return false;
            }
            BlockPos leftmostPos = currentPos;
            // No need to iterate over the nixies to the left again
            currentPos = start;
            while (true) {
                BlockPos nextPos = currentPos.offset(right);
                if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
                    break;
                if (world.getBlockEntity(nextPos) instanceof NixieTubeBlockEntity ntbe && AbstractComputerBehaviour.contains(ntbe)) {
                    return false;
                }
                currentPos = nextPos;
            }
            currentPos = leftmostPos;
        }

        int index = 0;

        while (true) {
            final int rowPosition = index;
            if (callback != null)
                callback.accept(currentPos, rowPosition);
            BlockPos nextPos = currentPos.offset(right);
            if (!areNixieBlocksEqual(world.getBlockState(nextPos), state))
                break;
            currentPos = nextPos;
            index++;
        }

        return true;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(FACE, FACING, WATERLOGGED));
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.ORANGE_NIXIE_TUBE.getDefaultStack();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return new ItemRequirement(ItemUseType.CONSUME, AllItems.ORANGE_NIXIE_TUBE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        Direction facing = pState.get(FACING);
        return switch (pState.get(FACE)) {
            case CEILING -> AllShapes.NIXIE_TUBE_CEILING.get(facing.rotateYClockwise().getAxis());
            case FLOOR -> AllShapes.NIXIE_TUBE.get(facing.rotateYClockwise().getAxis());
            default -> AllShapes.NIXIE_TUBE_WALL.get(facing);
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
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
        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null)
            return null;
        if (state.get(FACE) != DoubleAttachFace.WALL && state.get(FACE) != DoubleAttachFace.WALL_REVERSED)
            state = state.with(FACING, state.get(FACING).rotateYClockwise());
        return state.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World level,
        BlockPos pos,
        Block block,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (level.isClient())
            return;
        if (!level.getBlockTickScheduler().isTicking(pos, this))
            level.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
        updateDisplayedRedstoneValue(state, worldIn, pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() == oldState.getBlock() || isMoving || oldState.getBlock() instanceof NixieTubeBlock)
            return;
        if (Mods.COMPUTERCRAFT.isLoaded() && isInComputerControlledRow(worldIn, pos)) {
            // The nixie tube has been placed in a computer-controlled row.
            walkNixies(
                worldIn, pos, true, (currentPos, rowPosition) -> {
                    if (worldIn.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
                        ntbe.displayEmptyText(rowPosition);
                }
            );
            return;
        }
        updateDisplayedRedstoneValue(state, worldIn, pos);
    }

    public static void updateDisplayedRedstoneValue(NixieTubeBlockEntity be, BlockState state, boolean force) {
        if (be.getWorld() == null || be.getWorld().isClient())
            return;
        if (be.reactsToRedstone() || force)
            be.updateRedstoneStrength(getPower(be.getWorld(), state, be.getPos()));
    }

    private void updateDisplayedRedstoneValue(BlockState state, World level, BlockPos pos) {
        if (level.isClient())
            return;
        withBlockEntityDo(level, pos, be -> NixieTubeBlock.updateDisplayedRedstoneValue(be, state, false));
    }

    static boolean isValidBlock(BlockView world, BlockPos pos, boolean above) {
        BlockState state = world.getBlockState(pos.up(above ? 1 : -1));
        return !state.getOutlineShape(world, pos).isEmpty();
    }

    private static int getPower(World worldIn, BlockState state, BlockPos pos) {
        int power = 0;
        for (Direction direction : Iterate.directions)
            power = Math.max(worldIn.getEmittedRedstonePower(pos.offset(direction), direction), power);
        for (Direction direction : Iterate.directions) {
            if (state.get(FACING).getOpposite() != direction)
                power = Math.max(worldIn.getEmittedRedstonePower(pos.offset(direction), Direction.UP), power);
        }
        return power;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
        return side != null;
    }

    @Override
    public Class<NixieTubeBlockEntity> getBlockEntityClass() {
        return NixieTubeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NixieTubeBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.NIXIE_TUBE;
    }

    public DyeColor getColor() {
        return color;
    }

    public static NixieTubeBlock getColorBlock(DyeColor color) {
        return switch (color) {
            case WHITE -> AllBlocks.WHITE_NIXIE_TUBE;
            case ORANGE -> AllBlocks.ORANGE_NIXIE_TUBE;
            case MAGENTA -> AllBlocks.MAGENTA_NIXIE_TUBE;
            case LIGHT_BLUE -> AllBlocks.LIGHT_BLUE_NIXIE_TUBE;
            case YELLOW -> AllBlocks.YELLOW_NIXIE_TUBE;
            case LIME -> AllBlocks.LIME_NIXIE_TUBE;
            case PINK -> AllBlocks.PINK_NIXIE_TUBE;
            case GRAY -> AllBlocks.GRAY_NIXIE_TUBE;
            case LIGHT_GRAY -> AllBlocks.LIGHT_GRAY_NIXIE_TUBE;
            case CYAN -> AllBlocks.CYAN_NIXIE_TUBE;
            case PURPLE -> AllBlocks.PURPLE_NIXIE_TUBE;
            case BLUE -> AllBlocks.BLUE_NIXIE_TUBE;
            case BROWN -> AllBlocks.BROWN_NIXIE_TUBE;
            case GREEN -> AllBlocks.GREEN_NIXIE_TUBE;
            case RED -> AllBlocks.RED_NIXIE_TUBE;
            case BLACK -> AllBlocks.BLACK_NIXIE_TUBE;
        };
    }

    public static boolean areNixieBlocksEqual(BlockState blockState, BlockState otherState) {
        if (!(blockState.getBlock() instanceof NixieTubeBlock))
            return false;
        if (!(otherState.getBlock() instanceof NixieTubeBlock))
            return false;
        return withColor(blockState, DyeColor.WHITE) == withColor(otherState, DyeColor.WHITE);
    }

    public static BlockState withColor(BlockState state, DyeColor color) {
        return (color == DyeColor.ORANGE ? AllBlocks.ORANGE_NIXIE_TUBE : getColorBlock(color)).getDefaultState().with(FACING, state.get(FACING))
            .with(WATERLOGGED, state.get(WATERLOGGED)).with(FACE, state.get(FACE));
    }

    public static DyeColor colorOf(BlockState blockState) {
        return blockState.getBlock() instanceof NixieTubeBlock ? ((NixieTubeBlock) blockState.getBlock()).color : DyeColor.ORANGE;
    }

    public static Direction getFacing(BlockState sideState) {
        return getConnectedDirection(sideState);
    }
}
