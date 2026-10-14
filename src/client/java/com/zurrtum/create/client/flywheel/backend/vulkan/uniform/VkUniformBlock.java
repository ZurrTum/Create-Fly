package com.zurrtum.create.client.flywheel.backend.vulkan.uniform;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;

import com.zurrtum.create.client.flywheel.lib.util.ExtraMemoryOps;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * One std140 uniform block: a CPU staging buffer, and the slice of the shared
 * {@code GpuBuffer} that staging buffer is uploaded into.
 *
 * <p><b>Mirrors {@code backend.engine.uniform.UniformBuffer}, which this class does not
 * subclass.</b> That one binds to {@code GL_UNIFORM_BUFFER} by way of
 * {@code glBindBufferRange} and uploads through {@code GlBuffer}; both are OpenGL-only,
 * and it lives in a package this port does not own. The structure is deliberately the
 * same - allocate, mutate, {@code markDirty}, lazily upload - so the two read alike.
 *
 * <p>All six blocks live in one {@code GpuBuffer}. Vulkan binds each one through a
 * <em>dynamic offset</em> into that single buffer, which is exactly what VulkanMod's
 * {@code DescriptorSets.updateUniforms} expects from a UBO whose
 * {@code useGlobalBuffer} flag is set; see {@link VkUniformBuffer#bind}.
 */
public final class VkUniformBlock {

    private final VkUniformLayout.Block block;

    /** Direct buffer holding this frame's bytes, in std140 order. */
    private final ByteBuffer staging;
    private final long ptr;

    private boolean dirty = true;
    private long gpuOffset;
    private GpuBufferSlice slice;

    VkUniformBlock(VkUniformLayout.Block block) {
        this.block = block;
        this.staging = ByteBuffer.allocateDirect(block.size());
        this.ptr = MemoryUtil.memAddress(staging);
    }

    // ------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------

    public VkUniformLayout.Block block() {
        return block;
    }

    /** The GLSL block name, e.g. {@code _FlwFrameUniforms}. */
    public String name() {
        return block.name();
    }

    public int binding() {
        return block.binding();
    }

    /** Total std140 size in bytes. */
    public int size() {
        return block.size();
    }

    /** Byte offset of this block inside the shared {@code GpuBuffer}. */
    public long gpuOffset() {
        return gpuOffset;
    }

    /** Address of the staging memory, for bulk writes. */
    public long ptr() {
        return ptr;
    }

    /**
     * The GPU slice, or {@code null} before {@link #attach} has run (i.e. before
     * {@link VkUniformBuffer#allocate}).
     */
    public GpuBufferSlice slice() {
        return slice;
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /**
     * Places this block at {@code offset} inside {@code buffer}. Called once, by
     * {@link VkUniformBuffer#allocate}; {@code offset} must already be a multiple of
     * the device's uniform offset alignment.
     */
    void attach(GpuBuffer buffer, long offset) {
        this.gpuOffset = offset;
        this.slice = buffer.slice(offset, block.size());
        this.dirty = true;
    }

    public boolean dirty() {
        return dirty;
    }

    /** Marks the block for re-upload on the next {@link VkUniformBuffer#upload}. */
    public void markDirty() {
        dirty = true;
    }

    /** Clears the dirty flag after a successful upload. */
    void markClean() {
        dirty = false;
    }

    /**
     * Forgets the GPU slice, so a later {@link VkUniformBuffer#allocate} can attach a
     * fresh buffer without the old one being uploaded into.
     */
    void detach() {
        this.slice = null;
        this.gpuOffset = 0;
        this.dirty = true;
    }

    /** Zeroes the whole block and marks it dirty. */
    public void clear() {
        MemoryUtil.memSet(ptr, 0, block.size());
        markDirty();
    }

    /**
     * Copies {@code length} bytes of already-packed std140 data in from native memory.
     *
     * <p>This is the path that keeps Flywheel's existing uniform writers - {@code
     * FrameUniforms}, {@code PlayerUniforms}, and friends - as the single source of truth
     * for the five global blocks. They already produce correct std140 bytes into a
     * {@code MemoryBlock}; mirroring that memory is strictly safer than re-deriving the
     * values, and it means {@code backend/engine/uniform} needs no changes.
     *
     * @param srcPtr address of the source bytes
     * @param length how many to copy; clamped to this block's size
     */
    public void copyFrom(long srcPtr, int length) {
        int bytes = Math.min(length, block.size());
        MemoryUtil.memCopy(srcPtr, ptr, bytes);
        if (bytes < block.size()) {
            MemoryUtil.memSet(ptr + bytes, 0, block.size() - bytes);
        }
        markDirty();
    }

    /**
     * The staging bytes, rewound, ready for
     * {@link com.mojang.blaze3d.systems.CommandEncoder#writeToBuffer}.
     */
    ByteBuffer uploadView() {
        staging.clear();
        return staging;
    }

    // ------------------------------------------------------------------
    // Typed writers. Each writes exactly the bytes the GLSL member occupies,
    // including the implicit padding a std140 vec3 leaves behind.
    // ------------------------------------------------------------------

    public void putFloat(int offset, float value) {
        MemoryUtil.memPutFloat(ptr + offset, value);
        markDirty();
    }

    public void putInt(int offset, int value) {
        MemoryUtil.memPutInt(ptr + offset, value);
        markDirty();
    }

    public void putVec2(int offset, float x, float y) {
        MemoryUtil.memPutFloat(ptr + offset, x);
        MemoryUtil.memPutFloat(ptr + offset + 4, y);
        markDirty();
    }

    /**
     * Writes x, y, z and zeroes the fourth component, because std140 gives a vec3 a
     * 16-byte slot and the next member starts past it. Matches
     * {@code UniformWriter.writeVec3}, which does the same for the GL path.
     */
    public void putVec3(int offset, float x, float y, float z) {
        MemoryUtil.memPutFloat(ptr + offset, x);
        MemoryUtil.memPutFloat(ptr + offset + 4, y);
        MemoryUtil.memPutFloat(ptr + offset + 8, z);
        MemoryUtil.memPutFloat(ptr + offset + 12, 0f);
        markDirty();
    }

    public void putVec4(int offset, float x, float y, float z, float w) {
        MemoryUtil.memPutFloat(ptr + offset, x);
        MemoryUtil.memPutFloat(ptr + offset + 4, y);
        MemoryUtil.memPutFloat(ptr + offset + 8, z);
        MemoryUtil.memPutFloat(ptr + offset + 12, w);
        markDirty();
    }

    public void putUVec2(int offset, int x, int y) {
        MemoryUtil.memPutInt(ptr + offset, x);
        MemoryUtil.memPutInt(ptr + offset + 4, y);
        markDirty();
    }

    /** Writes 16 floats column-major, which is how joml and GLSL both store a mat4. */
    public void putMat4(int offset, Matrix4fc matrix) {
        ExtraMemoryOps.putMatrix4f(ptr + offset, matrix);
        markDirty();
    }

    /** Writes three columns of three floats, each padded to 16 bytes - std140's mat3. */
    public void putMat3(int offset, Matrix3fc matrix) {
        ExtraMemoryOps.putMatrix3fPadded(ptr + offset, matrix);
        markDirty();
    }

    @Override
    public String toString() {
        return "VkUniformBlock[" + block + "]";
    }
}