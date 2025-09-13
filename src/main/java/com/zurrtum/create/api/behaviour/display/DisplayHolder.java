package com.zurrtum.create.api.behaviour.display;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface DisplayHolder {
    NbtCompound getDisplayLinkData();

    void setDisplayLinkData(NbtCompound data);

    default void updateLine(int line, BlockPos pos) {
        NbtCompound data = getDisplayLinkData();
        if (data == null) {
            data = new NbtCompound();
            setDisplayLinkData(data);
        }
        data.put("Line" + line, BlockPos.CODEC, pos);
    }

    @Nullable
    default BlockPos getLine(int line) {
        NbtCompound data = getDisplayLinkData();
        if (data == null) {
            return null;
        }
        return data.get("Line" + line, BlockPos.CODEC).orElse(null);
    }

    default void removeLine(int line) {
        NbtCompound data = getDisplayLinkData();
        if (data == null) {
            return;
        }
        data.remove("Line" + line);
        if (data.isEmpty()) {
            setDisplayLinkData(null);
        }
    }

    default void writeDisplayLink(WriteView view) {
        NbtCompound data = getDisplayLinkData();
        if (data == null) {
            return;
        }
        view.put("DisplayLink", NbtCompound.CODEC, data);
    }

    default void readDisplayLink(ReadView view) {
        NbtCompound data = view.read("DisplayLink", NbtCompound.CODEC).orElse(null);
        setDisplayLinkData(data);
    }
}
