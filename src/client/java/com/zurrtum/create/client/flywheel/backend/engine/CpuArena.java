package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

public class CpuArena extends AbstractArena {

    private MemoryBlock memoryBlock;

    public CpuArena(long elementSizeBytes, int initialCapacity) {
        super(elementSizeBytes);

        memoryBlock = MemoryBlock.malloc(elementSizeBytes * initialCapacity);
    }

    public long indexToPointer(int i) {
        return memoryBlock.ptr() + i * elementSizeBytes;
    }

    public void delete() {
        memoryBlock.free();
    }

    public long byteCapacity() {
        return memoryBlock.size();
    }

    protected void grow() {
        memoryBlock = memoryBlock.realloc(memoryBlock.size() * 2);
    }
}
