package com.zurrtum.create.content.logistics.funnel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class AbstractDirectionalFunnelBlock extends AbstractFunnelBlock {

    public static final EnumProperty<Direction> FACING = Properties.FACING;

    protected AbstractDirectionalFunnelBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(FACING));
    }

    @Override
    protected Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

}
