package com.zurrtum.create.client.flywheel.api.material;

public enum WriteMask {
    COLOR_DEPTH, COLOR, DEPTH;

    private WriteMask() {
    }

    public boolean color() {
        return this == COLOR_DEPTH || this == COLOR;
    }

    public boolean depth() {
        return this == COLOR_DEPTH || this == DEPTH;
    }
}
