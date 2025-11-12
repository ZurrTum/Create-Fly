package com.zurrtum.create.content.kinetics.belt;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum BeltPart implements StringRepresentable {
    START,
    MIDDLE,
    END,
    PULLEY;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
