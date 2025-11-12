package com.zurrtum.create.content.trains.bogey;

import java.util.List;

import net.minecraft.resources.Identifier;

public record BogeySize(Identifier id, float wheelRadius) {
    public BogeySize nextBySize() {
        List<BogeySize> values = AllBogeySizes.allSortedIncreasing();
        int ordinal = values.indexOf(this);
        return values.get((ordinal + 1) % values.size());
    }
}