package com.zurrtum.create.client.flywheel.api.material;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;

public enum WriteMask {
    /**
     * Write to both the color and depth buffers.
     */
    COLOR_DEPTH,
    /**
     * Write to the color buffer only.
     */
    COLOR,
    /**
     * Write to the depth buffer only.
     */
    DEPTH;

    public int color() {
        if (this == COLOR_DEPTH || this == COLOR) {
            return ColorTargetState.WRITE_ALL;
        }
        return ColorTargetState.WRITE_NONE;
    }

    public boolean depth() {
        return this == COLOR_DEPTH || this == DEPTH;
    }
}
