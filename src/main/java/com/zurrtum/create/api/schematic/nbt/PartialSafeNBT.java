package com.zurrtum.create.api.schematic.nbt;

import net.minecraft.world.level.storage.ValueOutput;

public interface PartialSafeNBT {
    /**
     * This will always be called from the logical server
     */
    void writeSafe(ValueOutput view);
}
