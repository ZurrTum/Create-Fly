package com.zurrtum.create.content.equipment.clipboard;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ClipboardCloneable {
    String getClipboardKey();

    default boolean canWrite(HolderLookup.Provider registries, Direction side) {
        return true;
    }

    boolean writeToClipboard(ValueOutput view, Direction side);

    boolean readFromClipboard(ValueInput view, Player player, Direction side, boolean simulate);
}
