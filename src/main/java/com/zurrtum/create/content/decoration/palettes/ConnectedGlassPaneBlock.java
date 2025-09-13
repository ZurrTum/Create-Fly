package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class ConnectedGlassPaneBlock extends GlassPaneBlock {

    public ConnectedGlassPaneBlock(Settings builder) {
        super(builder);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (side.getAxis().isVertical())
            return adjacentBlockState == state;
        return super.isSideInvisible(state, adjacentBlockState, side);
    }

}
