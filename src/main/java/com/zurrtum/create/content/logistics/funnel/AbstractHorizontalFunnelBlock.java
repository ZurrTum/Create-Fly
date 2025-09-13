package com.zurrtum.create.content.logistics.funnel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public abstract class AbstractHorizontalFunnelBlock extends AbstractFunnelBlock {

    public static final EnumProperty<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;

    protected AbstractHorizontalFunnelBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(HORIZONTAL_FACING));
    }

    @Override
    protected Direction getFacing(BlockState state) {
        return state.get(HORIZONTAL_FACING);
    }

    @Override
    public BlockState rotate(BlockState p_185499_1_, BlockRotation p_185499_2_) {
        return p_185499_1_.with(HORIZONTAL_FACING, p_185499_2_.rotate(p_185499_1_.get(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState p_185471_1_, BlockMirror p_185471_2_) {
        return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.get(HORIZONTAL_FACING)));
    }

}
