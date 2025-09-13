package com.zurrtum.create.client.flywheel.backend.gl.buffer;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zurrtum.create.client.flywheel.backend.gl.GlObject;
import com.zurrtum.create.client.flywheel.lib.memory.FlwMemoryTracker;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

public class GlBuffer extends GlObject {
    protected final GlBufferUsage usage;
    /**
     * The size (in bytes) of the buffer on the GPU.
     */
    protected long size;

    public GlBuffer(GlBufferUsage usage) {
        handle(Buffer.IMPL.create());
        this.usage = usage;
    }

    public void upload(MemoryBlock memoryBlock) {
        upload(memoryBlock.ptr(), memoryBlock.size());
    }

    public void upload(long ptr, long size) {
        FlwMemoryTracker._freeGpuMemory(this.size);
        Buffer.IMPL.data(handle(), size, ptr, usage.glEnum);
        this.size = size;
        FlwMemoryTracker._allocGpuMemory(this.size);
    }

    public void uploadSpan(long offset, MemoryBlock memoryBlock) {
        uploadSpan(offset, memoryBlock.ptr(), memoryBlock.size());
    }

    public void uploadSpan(long offset, long ptr, long size) {
        Buffer.IMPL.subData(handle(), offset, size, ptr);
    }

    public long size() {
        return size;
    }

    protected void deleteInternal(int handle) {
        GlStateManager._glDeleteBuffers(handle);
        FlwMemoryTracker._freeGpuMemory(size);
    }
}
