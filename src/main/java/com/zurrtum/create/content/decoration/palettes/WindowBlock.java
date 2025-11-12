package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class WindowBlock extends ConnectedGlassBlock {

    protected final boolean translucent;

    public WindowBlock(Properties settings, boolean translucent) {
        super(settings);
        this.translucent = translucent;
    }

    public WindowBlock(Properties settings) {
        this(settings, false);
    }

    public static WindowBlock translucent(Properties settings) {
        return new WindowBlock(settings, true);
    }

    public boolean isTranslucent() {
        return translucent;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (state.getBlock() == adjacentBlockState.getBlock()) {
            return true;
        }
        if (state.getBlock() instanceof WindowBlock windowBlock && adjacentBlockState.getBlock() instanceof ConnectedGlassBlock) {
            return !windowBlock.isTranslucent() && side.getAxis().isHorizontal();
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

}
