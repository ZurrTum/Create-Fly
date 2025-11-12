package com.zurrtum.create.api.behaviour.display;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public interface DisplayHolder {
    CompoundTag getDisplayLinkData();

    void setDisplayLinkData(CompoundTag data);

    default void updateLine(int line, BlockPos pos) {
        CompoundTag data = getDisplayLinkData();
        if (data == null) {
            data = new CompoundTag();
            setDisplayLinkData(data);
        }
        data.store("Line" + line, BlockPos.CODEC, pos);
    }

    @Nullable
    default BlockPos getLine(int line) {
        CompoundTag data = getDisplayLinkData();
        if (data == null) {
            return null;
        }
        return data.read("Line" + line, BlockPos.CODEC).orElse(null);
    }

    default void removeLine(int line) {
        CompoundTag data = getDisplayLinkData();
        if (data == null) {
            return;
        }
        data.remove("Line" + line);
        if (data.isEmpty()) {
            setDisplayLinkData(null);
        }
    }

    default void writeDisplayLink(ValueOutput view) {
        CompoundTag data = getDisplayLinkData();
        if (data == null) {
            return;
        }
        view.store("DisplayLink", CompoundTag.CODEC, data);
    }

    default void readDisplayLink(ValueInput view) {
        CompoundTag data = view.read("DisplayLink", CompoundTag.CODEC).orElse(null);
        setDisplayLinkData(data);
    }
}
