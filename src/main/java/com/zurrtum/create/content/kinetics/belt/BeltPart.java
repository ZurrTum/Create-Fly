package com.zurrtum.create.content.kinetics.belt;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum BeltPart implements StringRepresentable {
    START, MIDDLE, END, PULLEY;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
