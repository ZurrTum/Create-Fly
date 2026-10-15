package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

/**
 * A second destination for the per-instance bytes that {@code InstancedInstancer} assembles
 * each frame.
 *
 * <p>The instancer's own {@code GlBuffer} is created inside {@code updateBuffer()} and is not
 * exposed, so a Vulkan backend that needs those same bytes to upload into a
 * {@code VkInstanceTexture} has no way in. Rather than reaching past the encapsuation, the
 * instancer is given an optional extra sink and writes to both.
 *
 * <p>This is the same split as {@link MeshBuffer}: the bytes are produced once, backend
 * agnosticly, and each backend is free to take a copy.
 *
 * <p>Uploads arrive in pairs - a full {@link #upload} when the buffer has to grow, and a
 * {@link #uploadSpan} for each run of changed instances - with the span offsets given in
 * <em>bytes</em>, exactly as {@code GlBuffer} expects them.
 */
public interface InstanceBuffer {
    /** Replace the whole buffer. Used when the backing storage has to grow. */
    void upload(MemoryBlock block);

    /** Overwrite {@code block.size()} bytes starting at byte {@code offset}. */
    void uploadSpan(long offset, MemoryBlock block);

    /** Release GPU resources. Idempotent. */
    void delete();

    /**
     * How many bytes of instance data this sink currently holds.
     *
     * <p>Needed because the instancer's growth decision reads its buffer's capacity, and on a
     * Vulkan-only path that buffer is this sink rather than the {@code GlBuffer} it would
     * otherwise consult.
     */
    long size();
}
