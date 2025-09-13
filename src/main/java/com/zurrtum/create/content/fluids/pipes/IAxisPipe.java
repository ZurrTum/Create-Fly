package com.zurrtum.create.content.fluids.pipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction.Axis;
import org.jetbrains.annotations.Nullable;

public interface IAxisPipe {

    @Nullable
    static Axis getAxisOf(BlockState state) {
        if (state.getBlock() instanceof IAxisPipe)
            return ((IAxisPipe) state.getBlock()).getAxis(state);
        return null;
    }

    Axis getAxis(BlockState state);

}
