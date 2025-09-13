package com.zurrtum.create.content.equipment.clipboard;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Direction;

public interface ClipboardCloneable {
    String getClipboardKey();

    default boolean canWrite(RegistryWrapper.WrapperLookup registries, Direction side) {
        return true;
    }

    boolean writeToClipboard(WriteView view, Direction side);

    boolean readFromClipboard(ReadView view, PlayerEntity player, Direction side, boolean simulate);
}
