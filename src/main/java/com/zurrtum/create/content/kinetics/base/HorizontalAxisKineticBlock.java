package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.WorldView;

public class HorizontalAxisKineticBlock extends KineticBlock {
    public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = Properties.HORIZONTAL_AXIS;

    public HorizontalAxisKineticBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_AXIS);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Axis preferredAxis = getPreferredHorizontalAxis(context);
        if (preferredAxis != null)
            return this.getDefaultState().with(HORIZONTAL_AXIS, preferredAxis);
        return this.getDefaultState().with(HORIZONTAL_AXIS, context.getHorizontalPlayerFacing().rotateYClockwise().getAxis());
    }

    public static Axis getPreferredHorizontalAxis(ItemPlacementContext context) {
        Direction prefferedSide = null;
        for (Direction side : Iterate.horizontalDirections) {
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos().offset(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(
                    context.getWorld(),
                    context.getBlockPos().offset(side),
                    blockState,
                    side.getOpposite()
                ))
                    if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
                        prefferedSide = null;
                        break;
                    } else {
                        prefferedSide = side;
                    }
            }
        }
        return prefferedSide == null ? null : prefferedSide.getAxis();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_AXIS);
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.get(HORIZONTAL_AXIS);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        Axis axis = state.get(HORIZONTAL_AXIS);
        return state.with(HORIZONTAL_AXIS, rot.rotate(Direction.get(AxisDirection.POSITIVE, axis)).getAxis());
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state;
    }

}
