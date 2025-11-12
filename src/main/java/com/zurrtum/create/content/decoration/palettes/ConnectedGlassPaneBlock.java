package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectedGlassPaneBlock extends GlassPaneBlock {

    public ConnectedGlassPaneBlock(Properties builder) {
        super(builder);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (side.getAxis().isVertical())
            return adjacentBlockState == state;
        return super.skipRendering(state, adjacentBlockState, side);
    }

}
