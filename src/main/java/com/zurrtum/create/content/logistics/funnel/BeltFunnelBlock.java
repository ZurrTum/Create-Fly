package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class BeltFunnelBlock extends AbstractHorizontalFunnelBlock implements SpecialBlockItemRequirement {

    private final FunnelBlock parent;

    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

    public enum Shape implements StringRepresentable {
        RETRACTED(AllShapes.BELT_FUNNEL_RETRACTED),
        EXTENDED(AllShapes.BELT_FUNNEL_EXTENDED),
        PUSHING(AllShapes.BELT_FUNNEL_PERPENDICULAR),
        PULLING(AllShapes.BELT_FUNNEL_PERPENDICULAR);

        final VoxelShaper shaper;

        Shape(VoxelShaper shaper) {
            this.shaper = shaper;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public BeltFunnelBlock(FunnelBlock parent, Properties p_i48377_1_) {
        super(p_i48377_1_);
        this.parent = parent;
        registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.RETRACTED));
    }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
        return AllBlockEntityTypes.FUNNEL.isValid(blockState);
    }

    public static BeltFunnelBlock andesite(Properties settings) {
        return new BeltFunnelBlock(AllBlocks.ANDESITE_FUNNEL, settings);
    }

    public static BeltFunnelBlock brass(Properties settings) {
        return new BeltFunnelBlock(AllBlocks.BRASS_FUNNEL, settings);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_.add(SHAPE));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    public boolean isOfSameType(FunnelBlock otherFunnel) {
        return parent == otherFunnel;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return state.getValue(SHAPE).shaper.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_, CollisionContext p_220071_4_) {
        if (p_220071_4_ instanceof EntityCollisionContext && ((EntityCollisionContext) p_220071_4_).getEntity() instanceof ItemEntity && (p_220071_1_.getValue(
            SHAPE) == Shape.PULLING || p_220071_1_.getValue(SHAPE) == Shape.PUSHING))
            return AllShapes.FUNNEL_COLLISION.get(getFacing(p_220071_1_));
        return getShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState stateForPlacement = super.getStateForPlacement(ctx);
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        Direction facing = ctx.getClickedFace().getAxis().isHorizontal() ? ctx.getClickedFace() : ctx.getHorizontalDirection();

        BlockState state = stateForPlacement.setValue(HORIZONTAL_FACING, facing);
        boolean sneaking = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
        return state.setValue(SHAPE, getShapeForPosition(world, pos, facing, !sneaking));
    }

    public static Shape getShapeForPosition(BlockGetter world, BlockPos pos, Direction facing, boolean extracting) {
        BlockPos posBelow = pos.below();
        BlockState stateBelow = world.getBlockState(posBelow);
        Shape perpendicularState = extracting ? Shape.PUSHING : Shape.PULLING;
        if (!stateBelow.is(AllBlocks.BELT))
            return perpendicularState;
        Direction movementFacing = stateBelow.getValue(BeltBlock.HORIZONTAL_FACING);
        return movementFacing.getAxis() != facing.getAxis() ? perpendicularState : Shape.RETRACTED;
    }

    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(parent);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState neighbour,
        RandomSource random
    ) {
        updateWater(world, tickView, state, pos);
        if (!isOnValidBelt(state, world, pos)) {
            BlockState parentState = ProperWaterloggedBlock.withWater(world, parent.defaultBlockState(), pos);
            if (state.getValueOrElse(POWERED, false))
                parentState = parentState.setValue(POWERED, true);
            if (state.getValue(SHAPE) == Shape.PUSHING)
                parentState = parentState.setValue(FunnelBlock.EXTRACTING, true);
            return parentState.setValue(FunnelBlock.FACING, state.getValue(HORIZONTAL_FACING));
        }
        Shape updatedShape = getShapeForPosition(world, pos, state.getValue(HORIZONTAL_FACING), state.getValue(SHAPE) == Shape.PUSHING);
        Shape currentShape = state.getValue(SHAPE);
        if (updatedShape == currentShape)
            return state;

        // Don't revert wrenched states
        if (updatedShape == Shape.PUSHING && currentShape == Shape.PULLING)
            return state;
        if (updatedShape == Shape.RETRACTED && currentShape == Shape.EXTENDED)
            return state;

        return state.setValue(SHAPE, updatedShape);
    }

    public static boolean isOnValidBelt(BlockState state, LevelReader world, BlockPos pos) {
        BlockState stateBelow = world.getBlockState(pos.below());
        if ((stateBelow.getBlock() instanceof BeltBlock))
            return BeltBlock.canTransportObjects(stateBelow);
        DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(world, pos.below(), DirectBeltInputBehaviour.TYPE);
        if (directBeltInputBehaviour == null)
            return false;
        return directBeltInputBehaviour.canSupportBeltFunnels();
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide())
            return InteractionResult.SUCCESS;

        Shape shape = state.getValue(SHAPE);
        Shape newShape = shape;
        if (shape == Shape.PULLING)
            newShape = Shape.PUSHING;
        else if (shape == Shape.PUSHING)
            newShape = Shape.PULLING;
        else if (shape == Shape.EXTENDED)
            newShape = Shape.RETRACTED;
        else if (shape == Shape.RETRACTED) {
            BlockState belt = world.getBlockState(context.getClickedPos().below());
            if (!(belt.getBlock() instanceof BeltBlock && belt.getValue(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL))
                newShape = Shape.EXTENDED;
        }

        if (newShape == shape)
            return InteractionResult.SUCCESS;

        world.setBlockAndUpdate(context.getClickedPos(), state.setValue(SHAPE, newShape));

        if (newShape == Shape.EXTENDED) {
            Direction facing = state.getValue(HORIZONTAL_FACING);
            BlockState opposite = world.getBlockState(context.getClickedPos().relative(facing));
            if (opposite.getBlock() instanceof BeltFunnelBlock && opposite.getValue(SHAPE) == Shape.EXTENDED && opposite.getValue(HORIZONTAL_FACING) == facing.getOpposite())
                AllAdvancements.FUNNEL_KISS.trigger((ServerPlayer) context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, parent.asItem());
    }

}
