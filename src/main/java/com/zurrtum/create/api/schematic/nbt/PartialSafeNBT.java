package com.zurrtum.create.api.schematic.nbt;

import net.minecraft.storage.WriteView;

public interface PartialSafeNBT {
    /**
     * This will always be called from the logical server
     */
    void writeSafe(WriteView view);
}
