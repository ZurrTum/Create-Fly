package com.zurrtum.create.content.kinetics.belt;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum BeltPart implements StringIdentifiable {
    START,
    MIDDLE,
    END,
    PULLEY;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
