package com.zurrtum.create.content.decoration.girder;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class GirderEncasedShaftBlock extends HorizontalAxisKineticBlock implements IBE<KineticBlockEntity>, Waterloggable, IWrenchable, SpecialBlockItemRequirement {

    public static final BooleanProperty TOP = GirderBlock.TOP;
    public static final BooleanProperty BOTTOM = GirderBlock.BOTTOM;

    public GirderEncasedShaftBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(WATERLOGGED, false).with(TOP, false).with(BOTTOM, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(TOP, BOTTOM, WATERLOGGED));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.GIRDER_BEAM_SHAFT.get(pState.get(HORIZONTAL_AXIS));
    }

    @Override
    public VoxelShape getSidesShape(BlockState pState, BlockView pReader, BlockPos pPos) {
        return VoxelShapes.union(super.getSidesShape(pState, pReader, pPos), AllShapes.EIGHT_VOXEL_POLE.get(Axis.Y));
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return AllBlocks.METAL_GIRDER.getDefaultState().with(WATERLOGGED, originalState.get(WATERLOGGED))
            .with(GirderBlock.X, originalState.get(HORIZONTAL_AXIS) == Axis.Z).with(GirderBlock.Z, originalState.get(HORIZONTAL_AXIS) == Axis.X)
            .with(GirderBlock.AXIS, originalState.get(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X)
            .with(GirderBlock.BOTTOM, originalState.get(BOTTOM)).with(GirderBlock.TOP, originalState.get(TOP));
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult onWrenched = super.onWrenched(state, context);
        PlayerEntity player = context.getPlayer();
        if (onWrenched == ActionResult.SUCCESS && player != null && !player.isCreative())
            player.getInventory().offerOrDrop(AllItems.SHAFT.getDefaultStack());
        return onWrenched;
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ENCASED_SHAFT;
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

        Property<Boolean> updateProperty = direction == Direction.UP ? TOP : BOTTOM;
        if (direction.getAxis().isVertical()) {
            if (world.getBlockState(pos.offset(direction)).getSidesShape(world, pos.offset(direction)).isEmpty())
                state = state.with(updateProperty, false);
            return GirderBlock.updateVerticalProperty(world, pos, state, updateProperty, neighbourState, direction);
        }

        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        FluidState ifluidstate = level.getFluidState(pos);
        BlockState state = super.getPlacementState(context);
        return state.with(WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), be).union(ItemRequirement.of(AllBlocks.METAL_GIRDER.getDefaultState(), be));
    }

}
