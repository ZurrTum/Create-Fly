package com.zurrtum.create.client.flywheel.backend.util;

import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import org.jetbrains.annotations.Nullable;

public class MemoryBuffer {
    private final long stride;

    @Nullable
    private MemoryBlock block;

    public MemoryBuffer(long stride) {
        this.stride = stride;
    }

    public boolean reallocIfNeeded(int index) {
        if (block == null) {
            block = MemoryBlock.malloc(neededCapacityForIndex(index + 8));
            return true;
        } else if (block.size() < neededCapacityForIndex(index)) {
            block = block.realloc(neededCapacityForIndex(index + 8));
            return true;
        }

        return false;
    }

    public long ptr() {
        return block.ptr();
    }

    public long ptrForIndex(int index) {
        return block.ptr() + bytePosForIndex(index);
    }

    public long bytePosForIndex(int index) {
        return index * stride;
    }

    public long neededCapacityForIndex(int index) {
        return (index + 1) * stride;
    }

    public void delete() {
        if (block != null) {
            block.free();
        }
    }
}
