package com.zurrtum.create.client.flywheel.backend.vulkan.buffer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.flywheel.backend.engine.MeshBuffer;
import com.zurrtum.create.client.flywheel.lib.memory.FlwMemoryTracker;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

/**
 * {@link MeshBuffer} backed by a single {@link GpuBuffer} for index data.
 */
public class VkIndexMeshBuffer implements MeshBuffer {

    private final String label;
    private final int usage;
    private GpuBuffer buffer;
    private long bytes;

    public VkIndexMeshBuffer() {
        this.label = "flywheel/index";
        this.usage = GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST;
    }

    @Override
    public void upload(MemoryBlock block) {
        long size = align4(block.size());
        if (size == 0) {
            return;
        }

        if (buffer == null || bytes < size) {
            reallocate(size);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(buffer.slice(0, block.size()), block.asBuffer());
    }

    @Override
    public void delete() {
        if (buffer != null) {
            FlwMemoryTracker._freeGpuMemory(bytes);
            buffer.close();
            buffer = null;
            bytes = 0;
        }
    }

    /**
     * Returns the underlying buffer for {@link RenderPass#setIndexBuffer}.
     */
    public GpuBuffer gpuBuffer() {
        return buffer;
    }

    private void reallocate(long size) {
        if (buffer != null) {
            FlwMemoryTracker._freeGpuMemory(bytes);
            buffer.close();
        }
        buffer = RenderSystem.getDevice().createBuffer(() -> label, usage, size);
        bytes = size;
        FlwMemoryTracker._allocGpuMemory(bytes);
    }

    private static long align4(long size) {
        return (size + 3L) & ~3L;
    }
}