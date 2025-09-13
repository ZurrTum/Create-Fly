package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.util.math.Direction;

public class ConnectedGlassBlock extends TransparentBlock {

    public ConnectedGlassBlock(Settings p_i48392_1_) {
        super(p_i48392_1_);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock || super.isSideInvisible(state, adjacentBlockState, side);
    }
}
