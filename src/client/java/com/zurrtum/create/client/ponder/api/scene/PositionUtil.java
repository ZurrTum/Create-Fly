package com.zurrtum.create.client.ponder.api.scene;

import net.minecraft.util.math.BlockPos;

public interface PositionUtil {
    BlockPos at(int x, int y, int z);

    BlockPos zero();
}