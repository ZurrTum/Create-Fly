package com.zurrtum.create.client.flywheel.backend.vulkan.buffer;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.backend.engine.InstanceBuffer;
import com.zurrtum.create.client.flywheel.lib.memory.MemoryBlock;

/**
 * {@link InstanceBuffer} implementation backed by {@link VkInstanceTexture}.
 *
 * <p>The shader expects the instance data as a flat word array in a 2D texture,
 * decoded by {@code _flw_texelFetchInstance} (see {@link VkInstanceTexture}).
 * The CPU-side byte layout is identical to the OpenGL {@code TextureBuffer} path,
 * so the same bytes from {@link InstancedInstancer} can be reused without any
 * reshuffling.
 */
public class VkInstanceBuffer implements InstanceBuffer {

    private final String label;
    private final int initialWordCapacity;
    private VkInstanceTexture texture;
    private boolean deleted;

    public VkInstanceBuffer(String label, int wordCapacity) {
        this.label = label;
        this.initialWordCapacity = wordCapacity;
    }

    private VkInstanceTexture ensureTexture(int minWords) {
        if (texture == null) {
            texture = new VkInstanceTexture(label, Math.max(initialWordCapacity, minWords));
        } else {
            texture.ensureCapacity(minWords);
        }
        return texture;
    }

    @Override
    public void upload(MemoryBlock block) {
        if (deleted) {
            return;
        }
        int words = Math.max(1, (int) (block.size() / VkInstanceTexture.BYTES_PER_WORD));
        ensureTexture(words).upload(0, block);
    }

    @Override
    public void uploadSpan(long offset, MemoryBlock block) {
        if (deleted) {
            return;
        }
        int firstWord = (int) (offset / VkInstanceTexture.BYTES_PER_WORD);
        int wordCount = (int) (block.size() / VkInstanceTexture.BYTES_PER_WORD);
        ensureTexture(firstWord + wordCount).upload(firstWord, block);
    }

    @Override
    public void delete() {
        deleted = true;
        if (texture != null) {
            texture.delete();
            texture = null;
        }
    }

    @Override
    public long size() {
        return texture == null ? 0L : (long) texture.capacityWords() * VkInstanceTexture.BYTES_PER_WORD;
    }

    /**
     * The texture view to bind for the sampler {@code _flw_instances}.
     */
    public GpuTextureView view() {
        if (deleted) {
            return null;
        }
        return ensureTexture(1).view();
    }

    /**
     * The nearest/clamp sampler that matches the shader's texelFetch usage.
     */
    public GpuSampler sampler() {
        return VkInstanceSampler.sampler();
    }
}