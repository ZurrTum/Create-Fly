package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

/**
 * A destination for the assembled vertex and index blocks that {@link MeshPool} and
 * {@link IndexPool} build each frame.
 *
 * <p>These two classes are backend-agnostic bookkeepers: they decide <em>what</em> bytes
 * exist and where each mesh sits inside them. They have no opinion on where those bytes end
 * up. Originally that destination was hard-wired to {@code GlBuffer}, which forced the
 * Vulkan backend to either bypass them and rebuild the arena layout, or carry a GL buffer
 * around it never touches.
 *
 * <p>Implementations own the lifetime of whatever they upload into. {@code delete()} is
 * called once when the owning pool is torn down, and must be safe to call on a buffer that
 * was never uploaded to.
 */
public interface MeshBuffer {
    /**
     * Replace the buffer's contents with {@code block}.
     *
     * <p>The block is freed by the caller immediately after this returns, so implementations
     * that need the data to outlive the call must copy it.
     */
    void upload(MemoryBlock block);

    /** Release GPU resources. Idempotent. */
    void delete();
}
