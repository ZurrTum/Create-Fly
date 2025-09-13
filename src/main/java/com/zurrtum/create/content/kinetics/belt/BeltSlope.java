package com.zurrtum.create.content.kinetics.belt;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum BeltSlope implements StringIdentifiable {
    HORIZONTAL,
    UPWARD,
    DOWNWARD,
    VERTICAL,
    SIDEWAYS;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isDiagonal() {
        return this == UPWARD || this == DOWNWARD;
    }
}
