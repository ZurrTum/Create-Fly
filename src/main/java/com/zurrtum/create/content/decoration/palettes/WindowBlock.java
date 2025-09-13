package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class WindowBlock extends ConnectedGlassBlock {

    protected final boolean translucent;

    public WindowBlock(Settings settings, boolean translucent) {
        super(settings);
        this.translucent = translucent;
    }

    public WindowBlock(Settings settings) {
        this(settings, false);
    }

    public static WindowBlock translucent(Settings settings) {
        return new WindowBlock(settings, true);
    }

    public boolean isTranslucent() {
        return translucent;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (state.getBlock() == adjacentBlockState.getBlock()) {
            return true;
        }
        if (state.getBlock() instanceof WindowBlock windowBlock && adjacentBlockState.getBlock() instanceof ConnectedGlassBlock) {
            return !windowBlock.isTranslucent() && side.getAxis().isHorizontal();
        }
        return super.isSideInvisible(state, adjacentBlockState, side);
    }

}
