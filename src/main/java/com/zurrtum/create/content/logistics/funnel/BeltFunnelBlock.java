package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Locale;

public class BeltFunnelBlock extends AbstractHorizontalFunnelBlock implements SpecialBlockItemRequirement {

    private final FunnelBlock parent;

    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);

    public enum Shape implements StringIdentifiable {
        RETRACTED(AllShapes.BELT_FUNNEL_RETRACTED),
        EXTENDED(AllShapes.BELT_FUNNEL_EXTENDED),
        PUSHING(AllShapes.BELT_FUNNEL_PERPENDICULAR),
        PULLING(AllShapes.BELT_FUNNEL_PERPENDICULAR);

        final VoxelShaper shaper;

        Shape(VoxelShaper shaper) {
            this.shaper = shaper;
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public BeltFunnelBlock(FunnelBlock parent, Settings p_i48377_1_) {
        super(p_i48377_1_);
        this.parent = parent;
        setDefaultState(getDefaultState().with(SHAPE, Shape.RETRACTED));
    }

    public static BeltFunnelBlock andesite(Settings settings) {
        return new BeltFunnelBlock(AllBlocks.ANDESITE_FUNNEL, settings);
    }

    public static BeltFunnelBlock brass(Settings settings) {
        return new BeltFunnelBlock(AllBlocks.BRASS_FUNNEL, settings);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> p_206840_1_) {
        super.appendProperties(p_206840_1_.add(SHAPE));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    public boolean isOfSameType(FunnelBlock otherFunnel) {
        return parent == otherFunnel;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return state.get(SHAPE).shaper.get(state.get(HORIZONTAL_FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockView p_220071_2_, BlockPos p_220071_3_, ShapeContext p_220071_4_) {
        if (p_220071_4_ instanceof EntityShapeContext && ((EntityShapeContext) p_220071_4_).getEntity() instanceof ItemEntity && (p_220071_1_.get(
            SHAPE) == Shape.PULLING || p_220071_1_.get(SHAPE) == Shape.PUSHING))
            return AllShapes.FUNNEL_COLLISION.get(getFacing(p_220071_1_));
        return getOutlineShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState stateForPlacement = super.getPlacementState(ctx);
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction facing = ctx.getSide().getAxis().isHorizontal() ? ctx.getSide() : ctx.getHorizontalPlayerFacing();

        BlockState state = stateForPlacement.with(HORIZONTAL_FACING, facing);
        boolean sneaking = ctx.getPlayer() != null && ctx.getPlayer().isSneaking();
        return state.with(SHAPE, getShapeForPosition(world, pos, facing, !sneaking));
    }

    public static Shape getShapeForPosition(BlockView world, BlockPos pos, Direction facing, boolean extracting) {
        BlockPos posBelow = pos.down();
        BlockState stateBelow = world.getBlockState(posBelow);
        Shape perpendicularState = extracting ? Shape.PUSHING : Shape.PULLING;
        if (!stateBelow.isOf(AllBlocks.BELT))
            return perpendicularState;
        Direction movementFacing = stateBelow.get(BeltBlock.HORIZONTAL_FACING);
        return movementFacing.getAxis() != facing.getAxis() ? perpendicularState : Shape.RETRACTED;
    }

    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(parent);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState neighbour,
        Random random
    ) {
        updateWater(world, tickView, state, pos);
        if (!isOnValidBelt(state, world, pos)) {
            BlockState parentState = ProperWaterloggedBlock.withWater(world, parent.getDefaultState(), pos);
            if (state.get(POWERED, false))
                parentState = parentState.with(POWERED, true);
            if (state.get(SHAPE) == Shape.PUSHING)
                parentState = parentState.with(FunnelBlock.EXTRACTING, true);
            return parentState.with(FunnelBlock.FACING, state.get(HORIZONTAL_FACING));
        }
        Shape updatedShape = getShapeForPosition(world, pos, state.get(HORIZONTAL_FACING), state.get(SHAPE) == Shape.PUSHING);
        Shape currentShape = state.get(SHAPE);
        if (updatedShape == currentShape)
            return state;

        // Don't revert wrenched states
        if (updatedShape == Shape.PUSHING && currentShape == Shape.PULLING)
            return state;
        if (updatedShape == Shape.RETRACTED && currentShape == Shape.EXTENDED)
            return state;

        return state.with(SHAPE, updatedShape);
    }

    public static boolean isOnValidBelt(BlockState state, WorldView world, BlockPos pos) {
        BlockState stateBelow = world.getBlockState(pos.down());
        if ((stateBelow.getBlock() instanceof BeltBlock))
            return BeltBlock.canTransportObjects(stateBelow);
        DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);
        if (directBeltInputBehaviour == null)
            return false;
        return directBeltInputBehaviour.canSupportBeltFunnels();
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient)
            return ActionResult.SUCCESS;

        Shape shape = state.get(SHAPE);
        Shape newShape = shape;
        if (shape == Shape.PULLING)
            newShape = Shape.PUSHING;
        else if (shape == Shape.PUSHING)
            newShape = Shape.PULLING;
        else if (shape == Shape.EXTENDED)
            newShape = Shape.RETRACTED;
        else if (shape == Shape.RETRACTED) {
            BlockState belt = world.getBlockState(context.getBlockPos().down());
            if (!(belt.getBlock() instanceof BeltBlock && belt.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL))
                newShape = Shape.EXTENDED;
        }

        if (newShape == shape)
            return ActionResult.SUCCESS;

        world.setBlockState(context.getBlockPos(), state.with(SHAPE, newShape));

        if (newShape == Shape.EXTENDED) {
            Direction facing = state.get(HORIZONTAL_FACING);
            BlockState opposite = world.getBlockState(context.getBlockPos().offset(facing));
            if (opposite.getBlock() instanceof BeltFunnelBlock && opposite.get(SHAPE) == Shape.EXTENDED && opposite.get(HORIZONTAL_FACING) == facing.getOpposite())
                AllAdvancements.FUNNEL_KISS.trigger((ServerPlayerEntity) context.getPlayer());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, parent.asItem());
    }

}
