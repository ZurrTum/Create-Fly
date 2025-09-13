package com.zurrtum.create.client.flywheel.lib.memory;

import com.zurrtum.create.client.flywheel.lib.util.StringUtil;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.atomic.AtomicLong;

public final class FlwMemoryTracker {
    private static final AtomicLong CPU_MEMORY = new AtomicLong(0);
    private static final AtomicLong GPU_MEMORY = new AtomicLong(0);

    private FlwMemoryTracker() {
    }

    public static long malloc(long size) {
        long ptr = MemoryUtil.nmemAlloc(size);
        if (ptr == MemoryUtil.NULL) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
        return ptr;
    }

    public static long calloc(long num, long size) {
        long ptr = MemoryUtil.nmemCalloc(num, size);
        if (ptr == MemoryUtil.NULL) {
            throw new OutOfMemoryError("Failed to allocate " + num + " elements of size " + size + " bytes");
        }
        return ptr;
    }

    public static long realloc(long ptr, long size) {
        long newPtr = MemoryUtil.nmemRealloc(ptr, size);
        if (newPtr == MemoryUtil.NULL) {
            throw new OutOfMemoryError("Failed to reallocate " + size + " bytes for address " + StringUtil.formatAddress(ptr));
        }
        return newPtr;
    }

    public static void free(long ptr) {
        MemoryUtil.nmemFree(ptr);
    }

    public static void _allocCpuMemory(long size) {
        CPU_MEMORY.getAndAdd(size);
    }

    public static void _freeCpuMemory(long size) {
        CPU_MEMORY.getAndAdd(-size);
    }

    public static void _allocGpuMemory(long size) {
        GPU_MEMORY.getAndAdd(size);
    }

    public static void _freeGpuMemory(long size) {
        GPU_MEMORY.getAndAdd(-size);
    }

    public static long getCpuMemory() {
        return CPU_MEMORY.get();
    }

    public static long getGpuMemory() {
        return GPU_MEMORY.get();
    }
}
