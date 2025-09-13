package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public abstract class RotatedPillarKineticBlock extends KineticBlock {

    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;

    public RotatedPillarKineticBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.get(AXIS)) {
                    case X:
                        return state.with(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.with(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    public static Axis getPreferredAxis(ItemPlacementContext context) {
        Axis prefferedAxis = null;
        for (Direction side : Iterate.directions) {
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos().offset(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(
                    context.getWorld(),
                    context.getBlockPos().offset(side),
                    blockState,
                    side.getOpposite()
                ))
                    if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                        prefferedAxis = null;
                        break;
                    } else {
                        prefferedAxis = side.getAxis();
                    }
            }
        }
        return prefferedAxis;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Axis preferredAxis = getPreferredAxis(context);
        if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer().isSneaking()))
            return this.getDefaultState().with(AXIS, preferredAxis);
        return this.getDefaultState().with(
            AXIS,
            preferredAxis != null && context.getPlayer().isSneaking() ? context.getSide().getAxis() : context.getPlayerLookDirection().getAxis()
        );
    }
}