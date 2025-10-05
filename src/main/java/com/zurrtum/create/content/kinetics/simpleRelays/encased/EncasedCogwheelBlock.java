package com.zurrtum.create.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.encasing.EncasedBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;

public class EncasedCogwheelBlock extends RotatedPillarKineticBlock implements ICogWheel, IBE<SimpleKineticBlockEntity>, SpecialBlockItemRequirement, TransformableBlock, EncasedBlock {

    public static final BooleanProperty TOP_SHAFT = BooleanProperty.of("top_shaft");
    public static final BooleanProperty BOTTOM_SHAFT = BooleanProperty.of("bottom_shaft");

    protected final boolean isLarge;
    private final Block casing;

    public EncasedCogwheelBlock(Settings properties, boolean large, Block casing) {
        super(properties);
        isLarge = large;
        this.casing = casing;
        setDefaultState(getDefaultState().with(TOP_SHAFT, false).with(BOTTOM_SHAFT, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(TOP_SHAFT, BOTTOM_SHAFT));
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return casing.asItem().getDefaultStack();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState placedOn = context.getWorld().getBlockState(context.getBlockPos().offset(context.getSide().getOpposite()));
        BlockState stateForPlacement = super.getPlacementState(context);
        if (ICogWheel.isSmallCog(placedOn))
            stateForPlacement = stateForPlacement.with(AXIS, ((IRotate) placedOn.getBlock()).getRotationAxis(placedOn));
        return stateForPlacement;
    }

    @Override
    public boolean isSideInvisible(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
        return pState.getBlock() == pAdjacentBlockState.getBlock() && pState.get(AXIS) == pAdjacentBlockState.get(AXIS);
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        if (context.getSide().getAxis() != state.get(AXIS))
            return super.onWrenched(state, context);

        World level = context.getWorld();
        if (level.isClient())
            return ActionResult.SUCCESS;

        BlockPos pos = context.getBlockPos();
        KineticBlockEntity.switchToBlockState(
            level,
            pos,
            state.cycle(context.getSide().getDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT)
        );
        IWrenchable.playRotateSound(level, pos);
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        originalState = swapShaftsForRotation(originalState, BlockRotation.CLOCKWISE_90, targetedFace.getAxis());
        return originalState.with(
            RotatedPillarKineticBlock.AXIS,
            VoxelShaper.axisAsFace(originalState.get(RotatedPillarKineticBlock.AXIS)).rotateClockwise(targetedFace.getAxis()).getAxis()
        );
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        if (context.getWorld().isClient())
            return ActionResult.SUCCESS;
        context.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, context.getBlockPos(), Block.getRawIdFromState(state));
        KineticBlockEntity.switchToBlockState(
            context.getWorld(),
            context.getBlockPos(),
            (isLarge ? AllBlocks.LARGE_COGWHEEL : AllBlocks.COGWHEEL).getDefaultState().with(AXIS, state.get(AXIS))
        );
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS) && state.get(face.getDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT);
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        if (newState.getBlock() instanceof EncasedCogwheelBlock && oldState.getBlock() instanceof EncasedCogwheelBlock) {
            if (newState.get(TOP_SHAFT) != oldState.get(TOP_SHAFT))
                return false;
            if (newState.get(BOTTOM_SHAFT) != oldState.get(BOTTOM_SHAFT))
                return false;
        }
        return super.areStatesKineticallyEquivalent(oldState, newState);
    }

    @Override
    public boolean isSmallCog() {
        return !isLarge;
    }

    @Override
    public boolean isLargeCog() {
        return isLarge;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        return CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.get(AXIS));
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    public BlockState swapShafts(BlockState state) {
        boolean bottom = state.get(BOTTOM_SHAFT);
        boolean top = state.get(TOP_SHAFT);
        state = state.with(BOTTOM_SHAFT, top);
        state = state.with(TOP_SHAFT, bottom);
        return state;
    }

    public BlockState swapShaftsForRotation(BlockState state, BlockRotation rotation, Direction.Axis rotationAxis) {
        if (rotation == BlockRotation.NONE) {
            return state;
        }

        Direction.Axis axis = state.get(AXIS);
        if (axis == rotationAxis) {
            return state;
        }

        if (rotation == BlockRotation.CLOCKWISE_180) {
            return swapShafts(state);
        }

        boolean clockwise = rotation == BlockRotation.CLOCKWISE_90;

        if (rotationAxis == Direction.Axis.X) {
            if (axis == Direction.Axis.Z && !clockwise || axis == Direction.Axis.Y && clockwise) {
                return swapShafts(state);
            }
        } else if (rotationAxis == Direction.Axis.Y) {
            if (axis == Direction.Axis.X && !clockwise || axis == Direction.Axis.Z && clockwise) {
                return swapShafts(state);
            }
        } else if (rotationAxis == Direction.Axis.Z) {
            if (axis == Direction.Axis.Y && !clockwise || axis == Direction.Axis.X && clockwise) {
                return swapShafts(state);
            }
        }

        return state;
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        Direction.Axis axis = state.get(AXIS);
        if (axis == Direction.Axis.X && mirror == BlockMirror.FRONT_BACK || axis == Direction.Axis.Z && mirror == BlockMirror.LEFT_RIGHT) {
            return swapShafts(state);
        }
        return state;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        state = swapShaftsForRotation(state, rotation, Direction.Axis.Y);
        return super.rotate(state, rotation);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Direction.Axis.Y) {
            return rotate(state, transform.rotation);
        }

        state = swapShaftsForRotation(state, transform.rotation, transform.rotationAxis);
        state = state.with(AXIS, transform.rotateAxis(state.get(AXIS)));
        return state;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(isLarge ? AllBlocks.LARGE_COGWHEEL.getDefaultState() : AllBlocks.COGWHEEL.getDefaultState(), be);
    }

    @Override
    public Class<SimpleKineticBlockEntity> getBlockEntityClass() {
        return SimpleKineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SimpleKineticBlockEntity> getBlockEntityType() {
        return isLarge ? AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL : AllBlockEntityTypes.ENCASED_COGWHEEL;
    }

    @Override
    public Block getCasing() {
        return casing;
    }

    @Override
    public void handleEncasing(BlockState state, World level, BlockPos pos, ItemStack heldItem, PlayerEntity player, Hand hand, BlockHitResult ray) {
        BlockState encasedState = getDefaultState().with(AXIS, state.get(AXIS));

        for (Direction d : Iterate.directionsInAxis(state.get(AXIS))) {
            BlockState adjacentState = level.getBlockState(pos.offset(d));
            if (!(adjacentState.getBlock() instanceof IRotate def))
                continue;
            if (!def.hasShaftTowards(level, pos.offset(d), adjacentState, d.getOpposite()))
                continue;
            encasedState = encasedState.cycle(d.getDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT : EncasedCogwheelBlock.BOTTOM_SHAFT);
        }

        KineticBlockEntity.switchToBlockState(level, pos, encasedState);
    }
}
