package com.zurrtum.create.client.flywheel.lib.model.baked;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.util.BufferAllocator;

class ByteBufferBuilderStack {
    private static final int INITIAL_CAPACITY_BYTES = 256 * 32;

    private int nextBufferBuilderIndex = 0;
    private final ReferenceArrayList<BufferAllocator> bufferBuilders = new ReferenceArrayList<>();

    BufferAllocator nextOrCreate() {
        BufferAllocator bufferBuilder;
        if (nextBufferBuilderIndex < bufferBuilders.size()) {
            bufferBuilder = bufferBuilders.get(nextBufferBuilderIndex);
        } else {
            // Need to allocate at least some memory up front, as BufferBuilder internally
            // only calls `ensureCapacity` after writing a vertex.
            bufferBuilder = new BufferAllocator(INITIAL_CAPACITY_BYTES);
            bufferBuilders.add(bufferBuilder);
        }
        nextBufferBuilderIndex++;
        return bufferBuilder;
    }

    public void reset() {
        nextBufferBuilderIndex = 0;
    }
}
