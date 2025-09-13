package com.zurrtum.create.client.foundation.block.connected;

import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.palettes.ConnectedPillarBlock;
import com.zurrtum.create.content.decoration.palettes.LayeredBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.BlockRenderView;

public class RotatedPillarCTBehaviour extends HorizontalCTBehaviour {

    public RotatedPillarCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
        super(layerShift, topShift);
    }

    @Override
    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockRenderView reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face,
        Direction primaryOffset,
        Direction secondaryOffset
    ) {
        if (other.getBlock() != state.getBlock())
            return false;
        Axis stateAxis = state.get(LayeredBlock.AXIS);
        if (other.get(LayeredBlock.AXIS) != stateAxis)
            return false;
        if (isBeingBlocked(state, reader, pos, otherPos, face))
            return false;
        if (reader.getBlockState(pos).getBlock() instanceof CopycatBlock)
            return true;
        if (reader.getBlockState(otherPos).getBlock() instanceof CopycatBlock)
            return true;
        if (primaryOffset != null && primaryOffset.getAxis() != stateAxis && !ConnectedPillarBlock.getConnection(state, primaryOffset))
            return false;
        if (secondaryOffset != null && secondaryOffset.getAxis() != stateAxis) {
            if (!ConnectedPillarBlock.getConnection(state, secondaryOffset))
                return false;
            return ConnectedPillarBlock.getConnection(other, secondaryOffset.getOpposite());
        }
        return true;
    }

    @Override
    protected boolean isBeingBlocked(BlockState state, BlockRenderView reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return state.get(LayeredBlock.AXIS) == face.getAxis() && super.isBeingBlocked(state, reader, pos, otherPos, face);
    }

    @Override
    protected boolean reverseUVs(BlockState state, Direction face) {
        Axis axis = state.get(LayeredBlock.AXIS);
        if (axis == Axis.X)
            return face.getDirection() == AxisDirection.NEGATIVE && face.getAxis() != Axis.X;
        if (axis == Axis.Z)
            return face != Direction.NORTH && face.getDirection() != AxisDirection.POSITIVE;
        return super.reverseUVs(state, face);
    }

    @Override
    protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
        return super.reverseUVsHorizontally(state, face);
    }

    @Override
    protected boolean reverseUVsVertically(BlockState state, Direction face) {
        Axis axis = state.get(LayeredBlock.AXIS);
        if (axis == Axis.X && face == Direction.NORTH)
            return false;
        if (axis == Axis.Z && face == Direction.WEST)
            return false;
        return super.reverseUVsVertically(state, face);
    }

    @Override
    protected Direction getUpDirection(BlockRenderView reader, BlockPos pos, BlockState state, Direction face) {
        Axis axis = state.get(LayeredBlock.AXIS);
        if (axis == Axis.Y)
            return super.getUpDirection(reader, pos, state, face);
        boolean alongX = axis == Axis.X;
        if (face.getAxis().isVertical() && alongX)
            return super.getUpDirection(reader, pos, state, face).rotateYClockwise();
        if (face.getAxis() == axis || face.getAxis().isVertical())
            return super.getUpDirection(reader, pos, state, face);
        return Direction.from(axis, alongX ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
    }

    @Override
    protected Direction getRightDirection(BlockRenderView reader, BlockPos pos, BlockState state, Direction face) {
        Axis axis = state.get(LayeredBlock.AXIS);
        if (axis == Axis.Y)
            return super.getRightDirection(reader, pos, state, face);
        if (face.getAxis().isVertical() && axis == Axis.X)
            return super.getRightDirection(reader, pos, state, face).rotateYClockwise();
        if (face.getAxis() == axis || face.getAxis().isVertical())
            return super.getRightDirection(reader, pos, state, face);
        return Direction.from(Axis.Y, face.getDirection());
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, Sprite sprite) {
        return super.getShift(state, direction.getAxis() == state.get(LayeredBlock.AXIS) ? Direction.UP : Direction.SOUTH, sprite);
    }

}
