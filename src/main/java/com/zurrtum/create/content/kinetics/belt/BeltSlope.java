package com.zurrtum.create.content.kinetics.belt;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

public enum BeltSlope implements StringRepresentable {
    HORIZONTAL,
    UPWARD,
    DOWNWARD,
    VERTICAL,
    SIDEWAYS;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isDiagonal() {
        return this == UPWARD || this == DOWNWARD;
    }
}
