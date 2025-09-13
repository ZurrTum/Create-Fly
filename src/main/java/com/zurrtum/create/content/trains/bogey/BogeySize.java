package com.zurrtum.create.content.trains.bogey;

import net.minecraft.util.Identifier;

import java.util.List;

public record BogeySize(Identifier id, float wheelRadius) {
    public BogeySize nextBySize() {
        List<BogeySize> values = AllBogeySizes.allSortedIncreasing();
        int ordinal = values.indexOf(this);
        return values.get((ordinal + 1) % values.size());
    }
}