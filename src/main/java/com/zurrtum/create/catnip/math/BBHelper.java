package com.zurrtum.create.catnip.math;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class BBHelper {

    public static BlockBox encapsulate(BlockBox bb, BlockPos pos) {
        return new BlockBox(
            Math.min(bb.getMinX(), pos.getX()),
            Math.min(bb.getMinY(), pos.getY()),
            Math.min(bb.getMinZ(), pos.getZ()),
            Math.max(bb.getMaxX(), pos.getX()),
            Math.max(bb.getMaxY(), pos.getY()),
            Math.max(bb.getMaxZ(), pos.getZ())
        );
    }

    public static BlockBox encapsulate(BlockBox bb, BlockBox bb2) {
        return new BlockBox(
            Math.min(bb.getMinX(), bb2.getMinX()),
            Math.min(bb.getMinY(), bb2.getMinY()),
            Math.min(bb.getMinZ(), bb2.getMinZ()),
            Math.max(bb.getMaxX(), bb2.getMaxX()),
            Math.max(bb.getMaxY(), bb2.getMaxY()),
            Math.max(bb.getMaxZ(), bb2.getMaxZ())
        );
    }

}
