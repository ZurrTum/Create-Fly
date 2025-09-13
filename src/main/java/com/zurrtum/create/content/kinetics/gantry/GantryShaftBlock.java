package com.zurrtum.create.content.kinetics.gantry;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class GantryShaftBlock extends DirectionalKineticBlock implements IBE<GantryShaftBlockEntity> {

    public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);
    public static final BooleanProperty POWERED = Properties.POWERED;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public enum Part implements StringIdentifiable {
        START,
        MIDDLE,
        END,
        SINGLE;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(PART, POWERED));
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
        if (!placementHelper.matchesItem(stack))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, ((BlockItem) stack.getItem()), player, hand);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.EIGHT_VOXEL_POLE.get(state.get(FACING).getAxis());
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbour,
        Random random
    ) {
        Direction facing = state.get(FACING);
        Axis axis = facing.getAxis();
        if (direction.getAxis() != axis)
            return state;
        boolean connect = neighbour.isOf(AllBlocks.GANTRY_SHAFT) && neighbour.get(FACING) == facing;

        Part part = state.get(PART);
        if (direction.getDirection() == facing.getDirection()) {
            if (connect) {
                if (part == Part.END)
                    part = Part.MIDDLE;
                if (part == Part.SINGLE)
                    part = Part.START;
            } else {
                if (part == Part.MIDDLE)
                    part = Part.END;
                if (part == Part.START)
                    part = Part.SINGLE;
            }
        } else {
            if (connect) {
                if (part == Part.START)
                    part = Part.MIDDLE;
                if (part == Part.SINGLE)
                    part = Part.END;
            } else {
                if (part == Part.MIDDLE)
                    part = Part.START;
                if (part == Part.END)
                    part = Part.SINGLE;
            }
        }

        return state.with(PART, part);
    }

    public GantryShaftBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false).with(PART, Part.SINGLE));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        Direction face = context.getSide();

        BlockState neighbour = world.getBlockState(pos.offset(state.get(FACING).getOpposite()));

        BlockState clickedState = neighbour.isOf(AllBlocks.GANTRY_SHAFT) ? neighbour : world.getBlockState(pos.offset(face.getOpposite()));

        if (clickedState.isOf(AllBlocks.GANTRY_SHAFT) && clickedState.get(FACING).getAxis() == state.get(FACING).getAxis()) {
            Direction facing = clickedState.get(FACING);
            state = state.with(FACING, context.getPlayer() == null || !context.getPlayer().isSneaking() ? facing : facing.getOpposite());
        }

        return state.with(POWERED, shouldBePowered(state, world, pos));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult onWrenched = super.onWrenched(state, context);
        if (onWrenched.isAccepted()) {
            BlockPos pos = context.getBlockPos();
            World world = context.getWorld();
            neighborUpdate(world.getBlockState(pos), world, pos, state.getBlock(), null, false);
        }
        return onWrenched;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);

        if (!worldIn.isClient() && oldState.isOf(AllBlocks.GANTRY_SHAFT)) {
            Part oldPart = oldState.get(PART), part = state.get(PART);
            if ((oldPart != Part.MIDDLE && part == Part.MIDDLE) || (oldPart == Part.SINGLE && part != Part.SINGLE)) {
                BlockEntity be = worldIn.getBlockEntity(pos);
                if (be instanceof GantryShaftBlockEntity)
                    ((GantryShaftBlockEntity) be).checkAttachedCarriageBlocks();
            }
        }
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable WireOrientation wireOrientation,
        boolean p_220069_6_
    ) {
        if (worldIn.isClient)
            return;
        boolean previouslyPowered = state.get(POWERED);
        boolean shouldPower = worldIn.isReceivingRedstonePower(pos); // shouldBePowered(state, worldIn, pos);

        if (!previouslyPowered && !shouldPower && shouldBePowered(state, worldIn, pos)) {
            worldIn.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
            return;
        }

        if (previouslyPowered == shouldPower)
            return;

        // Collect affected gantry shafts
        List<BlockPos> toUpdate = new ArrayList<>();
        Direction facing = state.get(FACING);
        Axis axis = facing.getAxis();
        for (Direction d : Iterate.directionsInAxis(axis)) {
            BlockPos currentPos = pos.offset(d);
            while (true) {
                if (!worldIn.isPosLoaded(currentPos))
                    break;
                BlockState currentState = worldIn.getBlockState(currentPos);
                if (!(currentState.getBlock() instanceof GantryShaftBlock))
                    break;
                if (currentState.get(FACING) != facing)
                    break;
                if (!shouldPower && currentState.get(POWERED) && worldIn.isReceivingRedstonePower(currentPos))
                    return;
                if (currentState.get(POWERED) == shouldPower)
                    break;
                toUpdate.add(currentPos);
                currentPos = currentPos.offset(d);
            }
        }

        toUpdate.add(pos);
        for (BlockPos blockPos : toUpdate) {
            BlockState blockState = worldIn.getBlockState(blockPos);
            BlockEntity be = worldIn.getBlockEntity(blockPos);
            if (be instanceof KineticBlockEntity)
                ((KineticBlockEntity) be).detachKinetics();
            if (blockState.getBlock() instanceof GantryShaftBlock)
                worldIn.setBlockState(blockPos, blockState.with(POWERED, shouldPower), Block.NOTIFY_LISTENERS);
        }
    }

    protected boolean shouldBePowered(BlockState state, World worldIn, BlockPos pos) {
        boolean shouldPower = worldIn.isReceivingRedstonePower(pos);

        Direction facing = state.get(FACING);
        for (Direction d : Iterate.directionsInAxis(facing.getAxis())) {
            BlockPos neighbourPos = pos.offset(d);
            if (!worldIn.isPosLoaded(neighbourPos))
                continue;
            BlockState neighbourState = worldIn.getBlockState(neighbourPos);
            if (!(neighbourState.getBlock() instanceof GantryShaftBlock))
                continue;
            if (neighbourState.get(FACING) != facing)
                continue;
            shouldPower |= neighbourState.get(POWERED);
        }

        return shouldPower;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(FACING).getAxis();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(FACING).getAxis();
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return super.areStatesKineticallyEquivalent(oldState, newState) && oldState.get(POWERED) == newState.get(POWERED);
    }

    @Override
    public float getParticleTargetRadius() {
        return .35f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .25f;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    public static class PlacementHelper extends PoleHelper<Direction> {

        public PlacementHelper() {
            super(state -> state.isOf(AllBlocks.GANTRY_SHAFT), s -> s.get(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.GANTRY_SHAFT);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
            offset.withTransform(offset.getTransform().andThen(s -> s.with(POWERED, state.get(POWERED))));
            return offset;
        }
    }

    @Override
    public Class<GantryShaftBlockEntity> getBlockEntityClass() {
        return GantryShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GantryShaftBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.GANTRY_SHAFT;
    }

}
