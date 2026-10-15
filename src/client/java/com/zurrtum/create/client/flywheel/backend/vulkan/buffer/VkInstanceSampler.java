package com.zurrtum.create.client.flywheel.backend.vulkan.buffer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;

import java.util.OptionalDouble;

/**
 * The single sampler Flywheel's buffer-texture replacement (Option C) samples with:
 * {@code NEAREST} + {@code CLAMP_TO_EDGE} on both axes.
 *
 * <p><b>Why nearest is mandatory.</b> {@code VkShaderSource#rewriteSamplerBuffers} rewrites
 * Flywheel's OpenGL {@code usamplerBuffer} reads into {@code sampler2D} +
 * {@code texelFetch(...)}. {@code texelFetch} ignores filtering entirely, but a sampler with
 * {@code minFilter/magFilter = LINEAR} on an integer texture is still an
 * error-checked-violation on strict drivers, and would silently interpolate the instance data
 * the moment anybody changed the fetch to {@code texture()}. Nearest is the only correct choice.
 *
 * <p><b>Why clamp.</b> The instance texture is addressed linearly as
 * {@code texelFetch(tex, ivec2(idx & MASK, idx >> SHIFT))}. Any out-of-range index that survives
 * the mask arithmetic must clamp rather than wrap, or an out-of-bounds instance read would
 * alias onto a valid instance instead of returning zero.
 *
 * <p><b>Why not {@code net.vulkanmod}.</b> The obvious VulkanMod route is
 * {@code SamplerManager.createTextureSampler(SamplerInfo.builder()....)} which returns a raw
 * {@code long} VkSampler handle. That handle is <em>not</em> what
 * {@link com.mojang.blaze3d.systems.RenderPass#bindTexture} wants - it wants a
 * {@link GpuSampler}, and {@code VkRenderPass.bindTexture} hard-casts its sampler argument to
 * {@code VkSampler}. Minecraft's own {@code GpuDevice.createSampler} is the public, stable,
 * backend-agnostic entry point that produces exactly that object, and
 * {@code VkGpuDevice.createSampler} implements it by delegating to VulkanMod. Using it keeps
 * {@code net.vulkanmod} out of this class entirely, so a VulkanMod upgrade only touches
 * {@link VkInstanceTexture}.
 *
 * <p><b>Ownership.</b> The sampler is process-lifetime, exactly like VulkanMod's own
 * {@code SamplerManager.getDefaultSampler()}: it is created lazily once and is deliberately
 * <em>not</em> closed by {@link VkInstanceTexture#delete()}, because closing it would invalidate
 * the handle for every other Flywheel buffer texture still alive.
 */
public final class VkInstanceSampler {

    private static GpuSampler sampler;

    private VkInstanceSampler() {
    }

    /**
     * Returns the shared nearest/clamp sampler, creating it on first use.
     *
     * <p>Must be called on the render thread, after {@code RenderSystem.getDevice()} is
     * non-null (i.e. not during mod construction).
     */
    public static GpuSampler sampler() {
        GpuSampler local = sampler;
        if (local == null) {
            local = RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.NEAREST,
                FilterMode.NEAREST,
                /* maxAnisotropy */ 0,
                OptionalDouble.empty()
            );
            sampler = local;
        }
        return local;
    }
}