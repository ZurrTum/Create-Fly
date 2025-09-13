package com.zurrtum.create.content.kinetics.chainDrive;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WeakPowerControlBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Locale;

public class ChainDriveBlock extends RotatedPillarKineticBlock implements IBE<KineticBlockEntity>, TransformableBlock, WeakPowerControlBlock {

    public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);
    public static final BooleanProperty CONNECTED_ALONG_FIRST_COORDINATE = DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

    public ChainDriveBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(PART, Part.NONE));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(PART, CONNECTED_ALONG_FIRST_COORDINATE));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Axis placedAxis = context.getPlayerLookDirection().getAxis();
        Axis axis = context.getPlayer() != null && context.getPlayer().isSneaking() ? placedAxis : getPreferredAxis(context);
        if (axis == null)
            axis = placedAxis;

        BlockState state = getDefaultState().with(AXIS, axis);
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis)
                continue;
            BlockPos pos = context.getBlockPos();
            BlockPos offset = pos.offset(facing);
            World world = context.getWorld();
            state = getStateForNeighborUpdate(state, world, world, pos, facing, offset, world.getBlockState(offset), world.getRandom());
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState stateIn,
        WorldView worldIn,
        ScheduledTickView tickView,
        BlockPos currentPos,
        Direction face,
        BlockPos facingPos,
        BlockState neighbour,
        Random random
    ) {
        Part part = stateIn.get(PART);
        Axis axis = stateIn.get(AXIS);
        boolean connectionAlongFirst = stateIn.get(CONNECTED_ALONG_FIRST_COORDINATE);
        Axis connectionAxis = connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);

        Axis faceAxis = face.getAxis();
        boolean facingAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
        boolean positive = face.getDirection() == AxisDirection.POSITIVE;

        if (axis == faceAxis)
            return stateIn;

        if (!(neighbour.getBlock() instanceof ChainDriveBlock)) {
            if (facingAlongFirst != connectionAlongFirst || part == Part.NONE)
                return stateIn;
            if (part == Part.MIDDLE)
                return stateIn.with(PART, positive ? Part.END : Part.START);
            if ((part == Part.START) == positive)
                return stateIn.with(PART, Part.NONE);
            return stateIn;
        }

        Part otherPart = neighbour.get(PART);
        Axis otherAxis = neighbour.get(AXIS);
        boolean otherConnection = neighbour.get(CONNECTED_ALONG_FIRST_COORDINATE);
        Axis otherConnectionAxis = otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);

        if (neighbour.get(AXIS) == faceAxis)
            return stateIn;
        if (otherPart != Part.NONE && otherConnectionAxis != faceAxis)
            return stateIn;

        if (part == Part.NONE) {
            part = positive ? Part.START : Part.END;
            connectionAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
        } else if (connectionAxis != faceAxis) {
            return stateIn;
        }

        if ((part == Part.START) != positive)
            part = Part.MIDDLE;

        return stateIn.with(PART, part).with(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (originalState.get(PART) == Part.NONE)
            return super.getRotatedBlockState(originalState, targetedFace);
        return super.getRotatedBlockState(originalState, Direction.get(AxisDirection.POSITIVE, getConnectionAxis(originalState)));
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, RedstoneView level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public BlockState updateAfterWrenched(BlockState newState, ItemUsageContext context) {
        //		Blocks.AIR.getDefaultState()
        //			.updateNeighbors(context.getWorld(), context.getPos(), 1);
        Axis axis = newState.get(AXIS);
        newState = getDefaultState().with(AXIS, axis);
        if (newState.contains(Properties.POWERED))
            newState = newState.with(Properties.POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()));
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis)
                continue;
            BlockPos pos = context.getBlockPos();
            BlockPos offset = pos.offset(facing);
            World world = context.getWorld();
            newState = getStateForNeighborUpdate(newState, world, world, pos, facing, offset, world.getBlockState(offset), world.getRandom());
        }
        //		newState.updateNeighbors(context.getWorld(), context.getPos(), 1 | 2);
        return newState;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    public static boolean areBlocksConnected(BlockState state, BlockState other, Direction facing) {
        Part part = state.get(PART);
        Axis connectionAxis = getConnectionAxis(state);
        Axis otherConnectionAxis = getConnectionAxis(other);

        if (otherConnectionAxis != connectionAxis)
            return false;
        if (facing.getAxis() != connectionAxis)
            return false;
        if (facing.getDirection() == AxisDirection.POSITIVE && (part == Part.MIDDLE || part == Part.START))
            return true;
        if (facing.getDirection() == AxisDirection.NEGATIVE && (part == Part.MIDDLE || part == Part.END))
            return true;

        return false;
    }

    protected static Axis getConnectionAxis(BlockState state) {
        Axis axis = state.get(AXIS);
        boolean connectionAlongFirst = state.get(CONNECTED_ALONG_FIRST_COORDINATE);
        return connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
    }

    public static float getRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to) {
        float fromMod = 1;
        float toMod = 1;
        if (from instanceof ChainGearshiftBlockEntity)
            fromMod = ((ChainGearshiftBlockEntity) from).getModifier();
        if (to instanceof ChainGearshiftBlockEntity)
            toMod = ((ChainGearshiftBlockEntity) to).getModifier();
        return fromMod / toMod;
    }

    public enum Part implements StringIdentifiable {
        START,
        MIDDLE,
        END,
        NONE;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
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
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return rotate(state, rot, Axis.Y);
    }

    protected BlockState rotate(BlockState pState, BlockRotation rot, Axis rotAxis) {
        Axis connectionAxis = getConnectionAxis(pState);
        Direction direction = Direction.from(connectionAxis, AxisDirection.POSITIVE);
        Direction normal = Direction.from(pState.get(AXIS), AxisDirection.POSITIVE);
        for (int i = 0; i < rot.ordinal(); i++) {
            direction = direction.rotateClockwise(rotAxis);
            normal = normal.rotateClockwise(rotAxis);
        }

        if (direction.getDirection() == AxisDirection.NEGATIVE)
            pState = reversePart(pState);

        Axis newAxis = normal.getAxis();
        Axis newConnectingDirection = direction.getAxis();
        boolean alongFirst = newAxis == Axis.X && newConnectingDirection == Axis.Y || newAxis != Axis.X && newConnectingDirection == Axis.X;

        return pState.with(AXIS, newAxis).with(CONNECTED_ALONG_FIRST_COORDINATE, alongFirst);
    }

    @Override
    public BlockState mirror(BlockState pState, BlockMirror pMirror) {
        Axis connectionAxis = getConnectionAxis(pState);
        if (pMirror.apply(Direction.from(connectionAxis, AxisDirection.POSITIVE)).getDirection() == AxisDirection.POSITIVE)
            return pState;
        return reversePart(pState);
    }

    protected BlockState reversePart(BlockState pState) {
        Part part = pState.get(PART);
        if (part == Part.START)
            return pState.with(PART, Part.END);
        if (part == Part.END)
            return pState.with(PART, Part.START);
        return pState;
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        return rotate(mirror(state, transform.mirror), transform.rotation, transform.rotationAxis);
    }

}
