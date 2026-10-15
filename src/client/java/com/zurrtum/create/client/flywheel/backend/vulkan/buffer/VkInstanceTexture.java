package com.zurrtum.create.client.flywheel.backend.vulkan.buffer;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.vulkan.shader.VkBindings;
import com.zurrtum.create.client.flywheel.lib.memory.FlwMemoryTracker;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Option C: the Vulkan replacement for OpenGL's {@code TextureBuffer} (a {@code GL_RGBA32UI}
 * texture buffer read with {@code usamplerBuffer} + {@code texelFetch}).
 *
 * <p><b>Why a plain 2D texture.</b> Vulkan has no texel buffers, and VulkanMod supports neither
 * {@code VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER} nor {@code VK_DESCRIPTOR_TYPE_STORAGE_BUFFER}.
 * The instance data is therefore stored as a {@code sampler2D} of
 * {@code VK_FORMAT_R8G8B8A8_UNORM} (one 4-byte word per texel) and reassembled in the shader by
 * {@code packUnorm4x8}.
 *
 * <p><b>Byte layout - this is the contract with {@code VkShaderSource}.</b> The shader helper
 * inserted by {@code VkShaderSource#rewriteSamplerBuffers} is
 * <pre>{@code
 * uvec4 _flw_texelFetchInstance(sampler2D tex, int idx) {
 *     int b = idx << 2;
 *     return uvec4(packUnorm4x8(texelFetch(tex, ivec2(b & 1023, b >> 10), 0)), ... b + 3 ...);
 * }
 * }</pre>
 * so, for a flat word array:
 * <ul>
 *   <li>word {@code i} lives at texel {@code (i & 1023, i >> 10)} - row-major, 1024 texels
 *       (4096 bytes) per row;</li>
 *   <li>the four bytes of a word are the texel's R, G, B, A channels in that order, because
 *       {@code packUnorm4x8} packs {@code x | y << 8 | z << 16 | w << 24} - i.e. plain
 *       little-endian;</li>
 *   <li>an OpenGL texel index {@code t} (which held 4 words in {@code GL_RGBA32UI}) is now four
 *       consecutive texels {@code 4t .. 4t + 3}, which is exactly what {@code idx << 2}
 *       computes. Callers that still think in GL texels must multiply by 4 first.</li>
 * </ul>
 * The {@link #upload(int, MemoryBlock)} overload takes a <em>word</em> offset, which is the unit
 * {@code BufferTextureInstanceComponent} indexes by ({@code base = instance * texels}).
 *
 * <p>The class is also usable for {@code _flw_lightLut} / {@code _flw_lightSections}, which decode
 * through {@code _flw_texelFetchU32} - one word per texel on both sides of the port.
 *
 * <p><b>Ownership / lifetime.</b> This class owns the whole chain
 * {@code VulkanImage -> VkGpuTexture -> VkTextureView}. {@link #delete()} closes the view, which
 * in turn closes the texture, which schedules the image for free through VulkanMod's
 * {@code MemoryManager}. Nothing outside this file ever sees a {@code net.vulkanmod} type.
 *
 * <p><b>Upload timing.</b> {@code VulkanImage.uploadSubTextureAsync} records into VulkanMod's
 * dedicated {@code ImageUploadHelper} command buffer, which is flushed at the head of
 * {@code Minecraft.runTick()}. An upload issued during frame N's world render therefore becomes
 * visible to frame N+1. That is VulkanMod's own behaviour for every Minecraft texture upload and is
 * not something this layer can fix; call {@link #flushUploads()} right after a batch of uploads if
 * the extra frame of latency matters.
 */
public class VkInstanceTexture {

    /** Texels per row. Shared with the shader-side decode helper. */
    public static final int WIDTH = VkBindings.BUFFER_TEXTURE_WIDTH;
    public static final int SHIFT = VkBindings.BUFFER_TEXTURE_SHIFT;
    public static final int MASK = VkBindings.BUFFER_TEXTURE_MASK;

    /** Bytes per texel, i.e. per 32-bit word. */
    public static final int BYTES_PER_WORD = 4;

    /** {@code VK_FORMAT_R8G8B8A8_UNORM}. */
    public static final int VK_FORMAT_RGBA8_UNORM = 37;

    /**
     * {@code VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT} (0x2 | 0x4).
     *
     * <p>{@code TRANSFER_DST} is mandatory: {@code uploadSubTextureAsync} transitions the image to
     * {@code VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL} before every copy. {@code SAMPLED} is mandatory:
     * the image view ends up in a combined-image-sampler descriptor.
     *
     * <p>For reference, Beryl's {@code setUsage(12)} is {@code STORAGE | SAMPLED} (its bloom
     * images are written by a compute pass); VulkanMod's own
     * {@code VulkanImage.createWhiteTexture} uses {@code 6}, the same value as here. The usage mask
     * is passed straight through to {@code VkImageCreateInfo::usage} by
     * {@code MemoryManager.createImage}, so these are raw {@code VkImageUsageFlags}.
     */
    public static final int VK_IMAGE_USAGE = 2 | 4;

    private final String label;

    private VulkanImage image;
    private GpuTexture texture;
    private GpuTextureView view;

    /** Total number of addressable words; always a multiple of {@link #WIDTH}. */
    private int capacityWords;

    private long gpuBytes;

    private boolean deleted;

    /** Instrumentation: {@code uploadSubTextureAsync} calls / words since the last reset. */
    private int uploadCalls;
    private long uploadWords;

    public VkInstanceTexture(String label, int wordCapacity) {
        this.label = label;

        create(Math.max(1, (wordCapacity + WIDTH - 1) / WIDTH));
    }

    private void create(int rows) {
        GpuDevice device = RenderSystem.getDevice();
        int maxSize = Math.max(1, device.getMaxTextureSize());
        if (rows > maxSize) {
            FlwBackend.LOGGER.error(
                "[FW/VK] {} wants {} rows of {} texels but this device caps textures at {}; "
                    + "instance data past that point will be dropped.",
                label, rows, WIDTH, maxSize);
            rows = maxSize;
        }
        rows = Math.max(1, rows);

        VulkanImage created = VulkanImage.builder(WIDTH, rows)
            .setName("flywheel/" + label)
            .setFormat(VK_FORMAT_RGBA8_UNORM)
            .setUsage(VK_IMAGE_USAGE)
            // Read with texelFetch; an explicit nearest/clamp GpuSampler is bound at draw time.
            .setLinearFiltering(false)
            .setClamp(true)
            .createVulkanImage();

        GpuTexture createdTexture;
        GpuTextureView createdView;
        try {
            createdTexture = ((VkGpuDevice) device).gpuTextureFromVulkanImage(created);
            createdView = device.createTextureView(createdTexture);
        } catch (Throwable t) {
            created.free();
            throw t;
        }

        this.image = created;
        this.texture = createdTexture;
        this.view = createdView;

        this.capacityWords = rows * WIDTH;
        this.gpuBytes = (long) rows * WIDTH * BYTES_PER_WORD;

        FlwMemoryTracker._allocGpuMemory(gpuBytes);
    }

    /**
     * Grows the texture so it can hold at least {@code wordCapacity} words.
     *
     * <p>A Vulkan image cannot be resized, so growing allocates a fresh one and the previous
     * contents are lost. Callers mirroring {@code InstancedInstancer} should treat a {@code true}
     * return as "re-upload everything", which is exactly what the GL path already does when its VBO
     * has to grow.
     *
     * @return {@code true} if the backing image was replaced
     */
    public boolean ensureCapacity(int wordCapacity) {
        if (deleted) {
            throw new IllegalStateException("VkInstanceTexture " + label + " has been deleted");
        }
        if (wordCapacity <= capacityWords) {
            return false;
        }

        int rows = Math.max(1, (wordCapacity + WIDTH - 1) / WIDTH);
        release();
        create(rows);
        return true;
    }

    /** The view to hand to {@code RenderPass.bindTexture(name, view, sampler)}. */
    public GpuTextureView view() {
        return view;
    }

    public GpuSampler sampler() {
        return VkInstanceSampler.sampler();
    }

    /** Number of 32-bit words the texture can currently hold. Always a multiple of {@link #WIDTH}. */
    public int capacityWords() {
        return capacityWords;
    }

    public int width() {
        return WIDTH;
    }

    public int height() {
        return capacityWords / WIDTH;
    }

    public long gpuBytes() {
        return gpuBytes;
    }

    public String label() {
        return label;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Uploads {@code block.size() / 4} words starting at word {@code firstWord}.
     *
     * <p>The source data must be tightly packed 32-bit little-endian words, which is exactly what
     * {@code InstanceWriter} produces and what the GL backend hands to
     * {@code glTexSubImage2D} - the CPU-side byte layout is unchanged from the GL path.
     */
    public void upload(int firstWord, MemoryBlock block) {
        upload(firstWord, block.ptr(), (int) (block.size() / BYTES_PER_WORD));
    }

    /**
     * Uploads {@code wordCount} words of tightly packed data from native memory at {@code ptr}.
     *
     * <p>No copy is made on this side: {@code VulkanImage.uploadSubTextureAsync} memcpy's out of
     * {@code ptr} synchronously into its staging buffer, so {@code ptr} only has to stay valid for
     * the duration of this call.
     */
    public void upload(int firstWord, long ptr, int wordCount) {
        if (deleted || wordCount <= 0) {
            return;
        }
        int lastWord = firstWord + wordCount;
        if (firstWord < 0 || lastWord > capacityWords) {
            throw new IllegalArgumentException(
                "Upload [" + firstWord + ", " + lastWord + ") is outside " + label
                    + " capacity of " + capacityWords + " words");
        }

        record(ptr, firstWord, wordCount);
    }

    /**
     * Uploads {@code data.remaining() / 4} words starting at word {@code firstWord}. The buffer's
     * position and limit are respected; the buffer is not modified.
     */
    public void upload(int firstWord, ByteBuffer data) {
        if (deleted || data == null || !data.hasRemaining()) {
            return;
        }
        int words = data.remaining() / BYTES_PER_WORD;
        upload(firstWord, MemoryUtil.memAddress(data) + data.position(), words);
    }

    /**
     * Splits {@code [firstWord, firstWord + wordCount)} into whole-row bands (one copy each,
     * tightly packed) plus at most one partial row at each end, and issues one
     * {@code vkCmdCopyBufferToImage} per band.
     *
     * <p>The parameter order is fixed by {@code VulkanImage.uploadSubTextureAsync}'s nine-int
     * overload, which forwards to
     * {@code ImageUtil.copyBufferToImageCmd(stack, commandBuffer, buffer, image, arrayLayer,
     * mipLevel, width, height, xOffset, yOffset, bufferOffset, bufferRowLength,
     * bufferImageHeight)}:
     * <pre>
     *   0  mipLevel        always 0 (this texture has no mips)
     *   1  arrayLayer      always 0
     *   2  width           texels in this band
     *   3  height          rows in this band
     *   4  xOffset         destination x, in texels
     *   5  yOffset         destination y, in rows
     *   6  skipRows        source row skip, always 0
     *   7  skipPixels      source column skip, always 0
     *   8  bufferRowLength source row stride in texels; must be &gt; 0
     *  (9) data pointer
     * </pre>
     * {@code bufferRowLength} may <em>not</em> be 0 even though the Vulkan spec permits it:
     * {@code uploadSubTextureAsync} derives its copy size as
     * {@code (bufferRowLength * height - skipPixels) * formatSize}, so a 0 there would silently
     * copy nothing. VulkanMod's own call sites always pass a non-zero value for the same reason.
     */
    private void record(long ptr, int firstWord, int wordCount) {
        // Register for VulkanMod's automatic TRANSFER_DST -> SHADER_READ_ONLY transition.
        // ImageUploadHelper.submitCommands() calls SpriteUpdateUtil.transitionLayouts() *after* all
        // recorded copies, so the barrier lands after the data inside the same submit. This is the
        // same dance net.vulkanmod.vulkan.texture.VTextureSelector#uploadSubTexture performs.
        SpriteUpdateUtil.addTransitionedLayout(image);

        int lastWord = firstWord + wordCount;
        int word = firstWord;

        while (word < lastWord) {
            int x = word & MASK;
            int y = word >> SHIFT;
            int remaining = lastWord - word;
            long src = ptr + ((long) (word - firstWord) << 2);

            if (x == 0 && remaining >= WIDTH) {
                int rows = remaining / WIDTH;
                image.uploadSubTextureAsync(0, 0, WIDTH, rows, 0, y, 0, 0, WIDTH, src);
                uploadCalls++;
                uploadWords += (long) WIDTH * rows;
                word += rows * WIDTH;
            } else {
                int texels = Math.min(WIDTH - x, remaining);
                image.uploadSubTextureAsync(0, 0, texels, 1, x, y, 0, 0, texels, src);
                uploadCalls++;
                uploadWords += texels;
                word += texels;
            }
        }
    }

    /**
     * Submits VulkanMod's pending texture-upload command buffer immediately.
     *
     * <p>Optional. Without this, uploads issued during frame N are submitted at the head of frame
     * N+1, which is VulkanMod's default for every Minecraft texture upload. Calling it right after
     * a batch of uploads trades an extra queue submit for one frame less latency.
     */
    public void flushUploads() {
        ImageUploadHelper.INSTANCE.submitCommands();
    }

    public int uploadCalls() {
        return uploadCalls;
    }

    public long uploadWords() {
        return uploadWords;
    }

    public void resetCounters() {
        uploadCalls = 0;
        uploadWords = 0;
    }

    public void delete() {
        if (deleted) {
            return;
        }
        release();
        deleted = true;
    }

    private void release() {
        // VkTextureView.close() -> VkGpuTexture.close() -> VkGlTexture.glDeleteTextures ->
        // MemoryManager.addToFreeable(image). Both close()s are idempotent; closing the view is
        // sufficient, the texture close is belt-and-braces for a half-built object.
        FlwMemoryTracker._freeGpuMemory(gpuBytes);
        gpuBytes = 0;
        capacityWords = 0;

        if (view != null) {
            view.close();
            view = null;
        }
        if (texture != null) {
            texture.close();
            texture = null;
        }
        image = null;
    }
}