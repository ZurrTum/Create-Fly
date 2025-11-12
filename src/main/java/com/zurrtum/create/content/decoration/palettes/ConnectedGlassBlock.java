package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectedGlassBlock extends TransparentBlock {

    public ConnectedGlassBlock(Properties p_i48392_1_) {
        super(p_i48392_1_);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock || super.skipRendering(state, adjacentBlockState, side);
    }
}
