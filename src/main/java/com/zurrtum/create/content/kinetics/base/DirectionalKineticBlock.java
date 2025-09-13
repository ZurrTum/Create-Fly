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

public abstract class DirectionalKineticBlock extends KineticBlock {
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public DirectionalKineticBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    public Direction getPreferredFacing(ItemPlacementContext context) {
        Direction prefferedSide = null;
        for (Direction side : Iterate.directions) {
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isSneaking())) {
            Direction nearestLookingDirection = context.getPlayerLookDirection();
            return getDefaultState().with(
                FACING,
                context.getPlayer() != null && context.getPlayer().isSneaking() ? nearestLookingDirection : nearestLookingDirection.getOpposite()
            );
        }
        return getDefaultState().with(FACING, preferred.getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

}
