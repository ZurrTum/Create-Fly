package com.zurrtum.create.client.flywheel.backend.vulkan.uniform;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkProgram;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkUniformAccess;

import net.vulkanmod.render.engine.VkGpuBuffer;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.shader.descriptor.UBO;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Vulkan uniform layer: owns the six std140 blocks, packs writes into them, uploads
 * them once per frame, and points every pipeline's UBOs at the result.
 *
 * <p><b>Why a buffer and not a setter.</b> {@code GlProgram} forwards each
 * {@code glUniform*} straight to the driver, so a draw is a sequence of GL calls. Vulkan
 * has no equivalent: a uniform lives at {@code dynamicOffset + std140 offset} inside a
 * buffer the shader reads as one whole descriptor. So every {@code setXxx} here writes to
 * a native staging buffer, and {@link #upload(CommandEncoder)} pushes the dirty blocks to
 * the GPU once per frame. Call sites such as {@code InstancedDrawManager} and
 * {@code EmbeddedEnvironment} keep working unchanged - only the static type of "program"
 * changes, from {@code GlProgram} to {@code VkProgram}.
 *
 * <p><b>Layout comes from {@link VkUniformLayout}</b>, not from this class and not from
 * {@code VkBindings}: offsets are computed from the GLSL declaration order under std140
 * rules, and block sizes fall out of that computation.
 *
 * <h2>How often each block changes</h2>
 * <ul>
 *   <li>{@code _FlwDrawUniforms} is per-draw: {@code _flw_packedMaterial},
 *       {@code _flw_baseInstance} and the two embedding matrices are written between
 *       binds, many times per frame.</li>
 *   <li>{@code _FlwFrameUniforms}, {@code _FlwPlayerUniforms}, {@code _FlwLevelUniforms}
 *       and {@code _FlwFogUniforms} are rewritten every frame;
 *       {@code _FlwOptionsUniforms} only when the player touches the options screen.</li>
 * </ul>
 * Per-block dirty flags make an idle options block free, but the rest is one ~1.2 KB
 * upload per frame, which is not worth splitting up any further.
 *
 * <p><b>Not thread safe</b>, as {@link VkUniformAccess} requires: everything here runs on
 * the render thread.
 *
 * @see VkUniformBlock for the per-block staging buffer
 * @see VkDescriptorLayout for building the descriptors these blocks back
 */
public final class VkUniformBuffer implements VkUniformAccess {

    private static final int BUFFER_USAGE = GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST;

    private final Map<VkUniformLayout.Block, VkUniformBlock> blocks = new LinkedHashMap<>();
    private final Map<VkUniformBlock, Integer> blockOffsets = new LinkedHashMap<>();
    private final Map<String, Slot> slots = new HashMap<>();

    /**
     * Uniform names that have been written with a setter whose type does not match the
     * GLSL declaration, so the warning is logged once per name rather than once per draw.
     */
    private final Set<String> typeMismatches = new LinkedHashSet<>();

    private GpuBuffer buffer;
    private int totalSize;

    /**
     * A uniform name bound to the block it lives in and where in that block.
     */
    private record Slot(VkUniformBlock block, VkUniformLayout.Member member) {
    }

    public VkUniformBuffer() {
        for (VkUniformLayout.Block block : VkUniformLayout.ALL) {
            blocks.put(block, new VkUniformBlock(block));
        }
    }

    // ------------------------------------------------------------------
    // Block access
    // ------------------------------------------------------------------

    public VkUniformBlock frame() {
        return block(VkUniformLayout.FRAME);
    }

    public VkUniformBlock fog() {
        return block(VkUniformLayout.FOG);
    }

    public VkUniformBlock options() {
        return block(VkUniformLayout.OPTIONS);
    }

    public VkUniformBlock player() {
        return block(VkUniformLayout.PLAYER);
    }

    public VkUniformBlock level() {
        return block(VkUniformLayout.LEVEL);
    }

    public VkUniformBlock draw() {
        return block(VkUniformLayout.DRAW);
    }

    public VkUniformBlock block(VkUniformLayout.Block block) {
        return blocks.get(block);
    }

    /**
     * Looks a block up by GLSL name, or {@code null} if the name is not one of the six.
     *
     * <p>The intended use is the mirror path:
     * {@code uniforms.block("_FlwFrameUniforms").copyFrom(glBuffer.ptr(), glBuffer.byteSize())},
     * which keeps {@code FrameUniforms} and friends as the single source of truth for the
     * five global blocks. See {@link VkUniformBlock#copyFrom}.
     */
    public VkUniformBlock block(String blockName) {
        VkUniformLayout.Block block = VkUniformLayout.block(blockName);
        return block == null ? null : blocks.get(block);
    }

    // ------------------------------------------------------------------
    // GPU lifecycle
    // ------------------------------------------------------------------

    /**
     * Creates the single {@code GpuBuffer} backing all six blocks, laying them out at
     * {@code GpuDevice#getUniformOffsetAlignment()} boundaries - 256 bytes on most
     * desktop drivers - because VulkanMod hands each block's start to
     * {@code vkCmdBindDescriptorSets} as a dynamic offset, and Vulkan requires those to
     * be aligned.
     *
     * <p>Idempotent.
     */
    public void allocate(GpuDevice device) {
        if (buffer != null) {
            return;
        }

        int alignment = Math.max(16, device.getUniformOffsetAlignment());

        int offset = 0;
        for (VkUniformBlock block : blocks.values()) {
            offset = alignUp(offset, alignment);
            blockOffsets.put(block, offset);
            offset += block.size();
        }

        buffer = device.createBuffer(() -> "flywheel/uniforms", BUFFER_USAGE, offset);
        totalSize = offset;

        for (Map.Entry<VkUniformBlock, Integer> entry : blockOffsets.entrySet()) {
            entry.getKey().attach(buffer, entry.getValue());
        }
    }

    /** Bytes reserved in the shared buffer, including the alignment padding. */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Uploads every dirty block. Call once per frame, after the uniform writers have run
     * and before the first draw.
     */
    public void upload(CommandEncoder encoder) {
        if (buffer == null) {
            throw new IllegalStateException("VkUniformBuffer.upload() called before allocate()");
        }

        for (VkUniformBlock block : blocks.values()) {
            if (!block.dirty()) {
                continue;
            }
            encoder.writeToBuffer(block.slice(), block.uploadView());
            block.markClean();
        }
    }

    /**
     * Points every UBO of {@code program} at this buffer.
     *
     * <p>VulkanMod decides where a UBO's bytes come from from two pieces of state on the
     * {@code UBO} itself: its {@code useGlobalBuffer} flag and the {@code BufferSlice} it
     * holds. If the slice's buffer is still {@code null} at bind time, VulkanMod
     * overwrites the slice with its own shared uniform buffer and sets the flag itself -
     * after which our data would never be read. So we set both, and we do it before the
     * pipeline's first {@code bindDescriptorSets}: VulkanMod only rewrites a descriptor
     * set when an image descriptor changes, so a later switch would not be picked up.
     *
     * <p>Cheap enough to call before every draw. The draw code should call it right
     * before {@code Renderer#uploadAndBindUBOs}, which is the VulkanMod call that both
     * refreshes the descriptor offsets and issues the bind.
     */
    public void bind(VkProgram program) {
        Buffer vulkanBuffer = vulkanBuffer();

        for (VkUniformBlock block : blocks.values()) {
            UBO ubo = program.pipeline().getUBO(block.name());
            if (ubo == null) {
                continue;
            }
            ubo.getBufferSlice().set(vulkanBuffer, block.gpuOffset(), block.size());
            ubo.setUseGlobalBuffer(true);
        }
    }

    /**
     * The VulkanMod {@code Buffer} behind our {@code GpuBuffer}.
     *
     * <p>{@code BufferSlice.set} wants a VulkanMod buffer, and MC's {@code GpuBuffer}
     * exposes no handle accessor - only VulkanMod's {@code VkGpuBuffer} subclass can
     * reach the {@code VkBuffer}. The cast is therefore unavoidable unless we bypass
     * MC's buffer abstraction entirely, which would cost us
     * {@code CommandEncoder#writeToBuffer} as well.
     */
    private Buffer vulkanBuffer() {
        if (buffer instanceof VkGpuBuffer vk) {
            return vk.getBuffer();
        }
        throw new IllegalStateException(
            "VkUniformBuffer needs a VulkanMod GpuBuffer to hand to VkDescriptorLayout, got "
                + (buffer == null ? "none (allocate() has not run)" : buffer.getClass().getName())
        );
    }

    /**
     * Frees the GPU buffer. Staging buffers live as long as this object, so a level
     * reload wants a fresh {@code VkUniformBuffer} - the same way
     * {@code Uniforms#onReloadLevelRenderer} drops the GL ones.
     */
    public void close() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
        for (VkUniformBlock block : blocks.values()) {
            block.detach();
        }
    }

    // ------------------------------------------------------------------
    // VkUniformAccess
    // ------------------------------------------------------------------

    @Override
    public void setFloat(String name, float value) {
        Slot slot = slot(name);
        if (slot == null || !expectScalar(slot)) {
            return;
        }
        slot.block().putFloat(slot.member().offset(), value);
    }

    @Override
    public void setVec2(String name, float x, float y) {
        Slot slot = slot(name, VkUniformLayout.Type.VEC2);
        if (slot == null) {
            return;
        }
        slot.block().putVec2(slot.member().offset(), x, y);
    }

    @Override
    public void setVec3(String name, float x, float y, float z) {
        Slot slot = slot(name, VkUniformLayout.Type.VEC3);
        if (slot == null) {
            return;
        }
        slot.block().putVec3(slot.member().offset(), x, y, z);
    }

    @Override
    public void setVec4(String name, float x, float y, float z, float w) {
        Slot slot = slot(name, VkUniformLayout.Type.VEC4);
        if (slot == null) {
            return;
        }
        slot.block().putVec4(slot.member().offset(), x, y, z, w);
    }

    @Override
    public void setMat4(String name, Matrix4fc matrix) {
        Slot slot = slot(name, VkUniformLayout.Type.MAT4);
        if (slot == null) {
            return;
        }
        slot.block().putMat4(slot.member().offset(), matrix);
    }

    @Override
    public void setMat3(String name, Matrix3fc matrix) {
        Slot slot = slot(name, VkUniformLayout.Type.MAT3);
        if (slot == null) {
            return;
        }
        slot.block().putMat3(slot.member().offset(), matrix);
    }

    @Override
    public void setInt(String name, int value) {
        Slot slot = slot(name);
        if (slot == null || !expectScalar(slot)) {
            return;
        }
        slot.block().putInt(slot.member().offset(), value);
    }

    @Override
    public void setUInt(String name, int value) {
        // A uint and an int occupy the same four bytes, so writing through memPutInt is
        // correct for both and saves a reinterpret of the sign.
        setInt(name, value);
    }

    @Override
    public void setUVec2(String name, int x, int y) {
        Slot slot = slot(name, VkUniformLayout.Type.UVEC2);
        if (slot == null) {
            return;
        }
        slot.block().putUVec2(slot.member().offset(), x, y);
    }

    /**
     * Whether {@code name} is a member of one of the six blocks.
     *
     * <p>Overrides the interface's {@code always-true} default so {@code VkProgram} can
     * tell a genuinely unused uniform from one this layer failed to resolve. Flywheel
     * does have such a name: {@code InstancedDrawManager} calls
     * {@code setUInt("_flw_vertexOffset", ...)}, which no longer exists in the GLSL - it
     * was superseded by {@code _flw_baseVertex} in {@code _FlwDrawUniforms}.
     */
    @Override
    public boolean has(String name) {
        return VkUniformLayout.member(name) != null;
    }

    @Override
    public String toString() {
        return "VkUniformBuffer[" + totalSize + " bytes across " + blocks.size() + " blocks]";
    }

    // ------------------------------------------------------------------
    // Name resolution
    // ------------------------------------------------------------------

    /** Resolves without a type check, for the scalar setters. */
    private Slot slot(String name) {
        Slot cached = slots.get(name);
        if (cached != null) {
            return cached;
        }

        VkUniformLayout.Member member = VkUniformLayout.member(name);
        if (member == null) {
            return null;
        }

        Slot slot = new Slot(blocks.get(VkUniformLayout.block(member.block())), member);
        slots.put(name, slot);
        return slot;
    }

    /** Resolves, and requires the GLSL type to be exactly {@code expected}. */
    private Slot slot(String name, VkUniformLayout.Type expected) {
        Slot slot = slot(name);
        if (slot == null) {
            return null;
        }
        if (slot.member().type() != expected) {
            warnTypeMismatch(name, slot.member().type(), expected);
            return null;
        }
        return slot;
    }

    /**
     * Accepts any 4-byte scalar. {@code float} and {@code int}/{@code uint} share a bit
     * pattern, so a same-width mismatch is harmless and not worth warning about; anything
     * wider or narrower is a real bug and gets dropped with a warning.
     */
    private boolean expectScalar(Slot slot) {
        if (slot.member().type().isScalar()) {
            return true;
        }
        warnTypeMismatch(slot.member().name(), slot.member().type(), null);
        return false;
    }

    private void warnTypeMismatch(String name, VkUniformLayout.Type actual, VkUniformLayout.Type wanted) {
        if (!typeMismatches.add(name)) {
            return;
        }
        FlwBackend.LOGGER.warn(
            "[FW/VK] Uniform '{}' is declared '{}' in its block but was written as {}; write ignored.",
            name,
            actual.name().toLowerCase(Locale.ROOT),
            wanted == null ? "a scalar" : wanted.name().toLowerCase(Locale.ROOT)
        );
    }

    private static int alignUp(int value, int align) {
        return (value + align - 1) / align * align;
    }
}