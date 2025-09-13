package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class DirectionalAxisKineticBlock extends DirectionalKineticBlock implements TransformableBlock {

    public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = BooleanProperty.of("axis_along_first");

    public DirectionalAxisKineticBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(AXIS_ALONG_FIRST_COORDINATE);
        super.appendProperties(builder);
    }

    protected Direction getFacingForPlacement(ItemPlacementContext context) {
        Direction facing = context.getPlayerLookDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isSneaking())
            facing = facing.getOpposite();
        return facing;
    }

    protected boolean getAxisAlignmentForPlacement(ItemPlacementContext context) {
        return context.getHorizontalPlayerFacing().getAxis() == Axis.X;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = getFacingForPlacement(context);
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        boolean alongFirst = false;
        Axis faceAxis = facing.getAxis();

        if (faceAxis.isHorizontal()) {
            alongFirst = faceAxis == Axis.Z;
            Direction positivePerpendicular = faceAxis == Axis.X ? Direction.SOUTH : Direction.EAST;

            boolean shaftAbove = prefersConnectionTo(world, pos, Direction.UP, true);
            boolean shaftBelow = prefersConnectionTo(world, pos, Direction.DOWN, true);
            boolean preferLeft = prefersConnectionTo(world, pos, positivePerpendicular, false);
            boolean preferRight = prefersConnectionTo(world, pos, positivePerpendicular.getOpposite(), false);

            if (shaftAbove || shaftBelow || preferLeft || preferRight)
                alongFirst = faceAxis == Axis.X;
        }

        if (faceAxis.isVertical()) {
            alongFirst = getAxisAlignmentForPlacement(context);
            Direction prefferedSide = null;

            for (Direction side : Iterate.horizontalDirections) {
                if (!prefersConnectionTo(world, pos, side, true) && !prefersConnectionTo(world, pos, side.rotateYClockwise(), false))
                    continue;
                if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
                    prefferedSide = null;
                    break;
                }
                prefferedSide = side;
            }

            if (prefferedSide != null)
                alongFirst = prefferedSide.getAxis() == Axis.X;
        }

        return this.getDefaultState().with(FACING, facing).with(AXIS_ALONG_FIRST_COORDINATE, alongFirst);
    }

    protected boolean prefersConnectionTo(WorldView reader, BlockPos pos, Direction facing, boolean shaftAxis) {
        if (!shaftAxis)
            return false;
        BlockPos neighbourPos = pos.offset(facing);
        BlockState blockState = reader.getBlockState(neighbourPos);
        Block block = blockState.getBlock();
        return block instanceof IRotate && ((IRotate) block).hasShaftTowards(reader, neighbourPos, blockState, facing.getOpposite());
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        Axis pistonAxis = state.get(FACING).getAxis();
        boolean alongFirst = state.get(AXIS_ALONG_FIRST_COORDINATE);

        if (pistonAxis == Axis.X)
            return alongFirst ? Axis.Y : Axis.Z;
        if (pistonAxis == Axis.Y)
            return alongFirst ? Axis.X : Axis.Z;
        if (pistonAxis == Axis.Z)
            return alongFirst ? Axis.X : Axis.Y;

        throw new IllegalStateException("Unknown axis??");
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        if (rot.ordinal() % 2 == 1)
            state = state.cycle(AXIS_ALONG_FIRST_COORDINATE);
        return super.rotate(state, rot);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Direction.Axis.Y) {
            return rotate(state, transform.rotation);
        }

        Direction newFacing = transform.rotateFacing(state.get(FACING));
        if (transform.rotationAxis == newFacing.getAxis() && transform.rotation.ordinal() % 2 == 1) {
            state = state.cycle(AXIS_ALONG_FIRST_COORDINATE);
        }
        return state.with(FACING, newFacing);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

}
