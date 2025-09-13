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
import net.minecraft.util.math.Direction;

public abstract class HorizontalKineticBlock extends KineticBlock {

    public static final EnumProperty<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;

    public HorizontalKineticBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(HORIZONTAL_FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    public Direction getPreferredHorizontalFacing(ItemPlacementContext context) {
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
        return prefferedSide;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(HORIZONTAL_FACING)));
    }

}
