package com.zurrtum.create.client.flywheel.backend.vulkan.state;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.texture.SamplerInfo;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.texture.VulkanImage;

import org.jetbrains.annotations.Nullable;

/**
 * The one place that talks to VulkanMod.
 *
 * <p>{@code net.vulkanmod.*} appears nowhere else in Flywheel's Vulkan backend,
 * so a VulkanMod upgrade that moves or renames a bridge class is a single-file
 * edit here rather than a sweep across the state, shader and buffer layers.
 * Everything above this class works in terms of Flywheel's own types and GL
 * constants, which is the vocabulary the rest of the backend already speaks.
 *
 * <h2>What VRenderSystem actually is</h2>
 *
 * <p>It is not a command-stream emulator — there is no GL context. It is a
 * <em>shadow copy of pipeline state</em>: every setter is a bare
 * {@code putstatic} into a public field. Nothing is sent to the GPU here. The
 * state is read back later by
 * {@code PipelineState.getCurrentPipelineState(RenderPass)}, which folds
 * {@code depthTest}, {@code depthMask}, {@code depthFun}, {@code cull},
 * {@code topology}, {@code polygonMode}, {@code colorMask}, the blend info and
 * the logic-op into five packed ints, compares them against a cached
 * {@code currentState}, and hands back either the cached state object or a new
 * one. {@code Renderer.bindGraphicsPipeline} then asks the {@code GraphicsPipeline}
 * for the variant matching that state and skips {@code vkCmdBindPipeline}
 * entirely if the handle is already bound.
 *
 * <p>Two consequences shape how this class is written:
 *
 * <ul>
 *   <li><b>There is no reason to cache here.</b> The setters cost a field
 *       store. The expensive thing is a <em>state change</em>, and VulkanMod
 *       already deduplicates that. A local cache here could only desync from
 *       whatever else in the frame last touched {@code VRenderSystem}.</li>
 *   <li><b>Ordering is load-bearing.</b> State is sampled at
 *       {@code bindGraphicsPipeline} time, so every setter must run
 *       <em>before</em> the pipeline bind, not merely before the draw. See
 *       {@link VkMaterialState} for the ordering contract.</li>
 * </ul>
 *
 * <p>The one exception is polygon offset: {@code polygonOffset} and its
 * enable/disable pair <em>do</em> dirty-check internally and bottom out in
 * {@code Renderer.setDepthBias}, a {@code vkCmdSetDepthBias} dynamic state
 * recorded straight onto the command buffer. It is therefore not part of the
 * pipeline key and is applied on the spot.
 */
public final class VkRenderState {

    // ---- Vulkan enums, passed through verbatim by SamplerManager ----

    /** {@code VkSamplerAddressMode}: REPEAT, MIRRORED_REPEAT, CLAMP_TO_EDGE, CLAMP_TO_BORDER. */
    private static final int ADDRESS_MODE_REPEAT = 0;
    private static final int ADDRESS_MODE_CLAMP_TO_EDGE = 2;

    /** {@code VkFilter}: NEAREST, LINEAR. */
    private static final int FILTER_NEAREST = 0;
    private static final int FILTER_LINEAR = 1;

    private VkRenderState() {
    }

    // ------------------------------------------------------------------
    // Depth test
    // ------------------------------------------------------------------

    public static void enableDepthTest() {
        VRenderSystem.enableDepthTest();
    }

    public static void disableDepthTest() {
        VRenderSystem.disableDepthTest();
    }

    /**
     * @param depthFunc one of the {@link VkDepthFunc} constants — a GL enum in
     *                  {@code 512..519}, <em>not</em> a {@code VkCompareOp}.
     */
    public static void depthFunc(int depthFunc) {
        VRenderSystem.depthFunc(depthFunc);
    }

    public static void depthMask(boolean write) {
        VRenderSystem.depthMask(write);
    }

    // ------------------------------------------------------------------
    // Face culling
    // ------------------------------------------------------------------

    /**
     * Enables back-face culling.
     *
     * <p>VulkanMod encodes {@code cull == true} as {@code VK_CULL_MODE_BACK_BIT}
     * with no front-face or cull-face control exposed, which happens to match
     * the GL defaults Flywheel relies on ({@code GL_BACK}, counter-clockwise
     * front). Flywheel never sets {@code glFrontFace} or {@code glCullFace}
     * either, so nothing is lost.
     */
    public static void enableCull() {
        VRenderSystem.enableCull();
    }

    public static void disableCull() {
        VRenderSystem.disableCull();
    }

    // ------------------------------------------------------------------
    // Blending
    // ------------------------------------------------------------------

    public static void enableBlend() {
        VRenderSystem.enableBlend();
    }

    public static void disableBlend() {
        VRenderSystem.disableBlend();
    }

    /**
     * Sets separate colour and alpha blend factors.
     *
     * <p><b>Arguments are in GL order</b> — {@code (srcRgb, dstRgb, srcAlpha,
     * dstAlpha)} — deliberately, so that the OpenGL code in
     * {@code MaterialRenderState} transliterates line for line onto this method.
     * VulkanMod's own {@code blendFuncSeparate} takes them as
     * {@code (srcRgb, srcAlpha, dstRgb, dstAlpha)}; the transposition happens
     * here and nowhere else. See {@link VkBlendMode} for the evidence.
     *
     * @param srcRgb   {@link VkBlendMode} constant — a GL enum in {@code 0},
     *                  {@code 1} or {@code 768..776}
     * @param dstRgb   {@link VkBlendMode} constant
     * @param srcAlpha {@link VkBlendMode} constant
     * @param dstAlpha {@link VkBlendMode} constant
     */
    public static void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        VRenderSystem.blendFuncSeparate(srcRgb, srcAlpha, dstRgb, dstAlpha);
    }

    /**
     * Sets the blend equation for both colour and alpha.
     *
     * <p>Unused by Flywheel, which never calls {@code glBlendEquation};
     * VulkanMod's default is already {@code GL_FUNC_ADD}.
     *
     * @param blendOp a {@link VkBlendMode} equation constant.
     */
    public static void blendOp(int blendOp) {
        VRenderSystem.blendOp(blendOp);
    }

    // ------------------------------------------------------------------
    // Write masks
    // ------------------------------------------------------------------

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        VRenderSystem.colorMask(red, green, blue, alpha);
    }

    // ------------------------------------------------------------------
    // Polygon offset
    // ------------------------------------------------------------------

    /**
     * @param factor slope-scale factor, as in {@code glPolygonOffset}
     * @param units  constant units, as in {@code glPolygonOffset}
     */
    public static void polygonOffset(float factor, float units) {
        VRenderSystem.polygonOffset(factor, units);
    }

    public static void enablePolygonOffset() {
        VRenderSystem.enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        VRenderSystem.disablePolygonOffset();
    }

    // ------------------------------------------------------------------
    // Topology escape hatch
    // ------------------------------------------------------------------

    /**
     * Selects triangle topology.
     *
     * <p>Not called by {@link VkMaterialState}: VulkanMod's field already
     * defaults to triangles and Flywheel only ever draws triangles, so there is
     * nothing to set per draw. Exposed for the draw-manager to assert once at
     * the start of a pass in case some other caller has moved the topology.
     *
     * @param mode a GL primitive enum; {@code 4} is {@code GL_TRIANGLES}.
     */
    public static void setTopologyGL(int mode) {
        VRenderSystem.setPrimitiveTopologyGL(mode);
    }

    // ------------------------------------------------------------------
    // Textures and samplers
    // ------------------------------------------------------------------

    /**
     * Points a texture unit at an MC texture view.
     *
     * <p>This is the MC-side entry point, and it is the right one: VulkanMod
     * keeps its own array of {@code VulkanImage} per unit, but resolves them
     * from {@code VRenderSystem}'s {@code GpuTextureView}s in
     * {@code VTextureSelector.bindShaderTextures}, which runs as part of
     * binding. Going through the view keeps {@code baseMipLevel} and
     * {@code mipLevels} intact, and lets the descriptor layer deal with cube
     * views without this class knowing what a cube map is.
     *
     * <p>There is no "active texture unit" to select, as there is in GL.
     *
     * @param unit texture unit index; VulkanMod accepts {@code 0..11}
     *             ({@code VTextureSelector.SIZE}), anything else is logged and
     *             dropped.
     */
    public static void setShaderTexture(int unit, GpuTextureView view) {
        VRenderSystem.setShaderTexture(unit, view);
    }

    /**
     * Attaches a sampler to the image behind a texture.
     *
     * <p>Samplers are <b>not</b> pipeline state and not bound per unit on the
     * Vulkan side: {@code DescriptorSets} reads {@code image.getSampler()} while
     * writing the descriptor, so the sampler lives on the {@code VulkanImage}.
     * The upshot is that this must be re-asserted per draw — two materials
     * sharing a texture but disagreeing about {@code blur} will otherwise
     * inherit whichever was set last.
     *
     * @param texture  the texture whose image should carry the sampler
     * @param sampler  supplies address mode and anisotropy; may be
     *                 {@code null}, in which case VulkanMod's builder defaults
     *                 are used
     * @param maxLod   highest mip level the sampler may reach. Pinning this to
     *                 the view's base mip level is how a material turns
     *                 mipmapping off, standing in for the
     *                 {@code GL_TEXTURE_BASE_LEVEL} / {@code GL_TEXTURE_MAX_LEVEL}
     *                 pair that {@code MaterialRenderState.setupTexture} sets.
     * @param filter   {@link FilterMode#LINEAR} when the material blurs,
     *                 otherwise {@link FilterMode#NEAREST}
     */
    public static void setSampler(GpuTexture texture, @Nullable GpuSampler sampler, int maxLod, FilterMode filter) {
        if (!(texture instanceof VkGpuTexture vkTexture)) {
            return;
        }

        VulkanImage image = vkTexture.getVulkanImage();
        if (image == null) {
            return;
        }

        image.setSampler(SamplerManager.getSampler(buildSamplerInfo(sampler, maxLod, filter)));
    }

    private static SamplerInfo buildSamplerInfo(@Nullable GpuSampler sampler, int maxLod, FilterMode filter) {
        AddressMode addressMode = (sampler == null) ? AddressMode.REPEAT : sampler.getAddressModeU();
        int mode = (addressMode == AddressMode.CLAMP_TO_EDGE) ? ADDRESS_MODE_CLAMP_TO_EDGE : ADDRESS_MODE_REPEAT;
        int filtering = (filter == FilterMode.LINEAR) ? FILTER_LINEAR : FILTER_NEAREST;

        SamplerInfo.Builder builder = SamplerInfo.builder()
                // VulkanMod's builder applies one mode to both axes; Minecraft
                // only ever has REPEAT and CLAMP_TO_EDGE, and block textures use
                // the same mode on U and V, so nothing is lost in practice.
                .setAddressMode(mode)
                .setFiltering(filtering, filtering, filtering)
                .setMaxLod(maxLod);

        // setAnisotropy() flags anisotropy on unconditionally, so only call it
        // when there is actually anisotropy to ask for.
        if (sampler != null && sampler.getMaxAnisotropy() > 1) {
            builder.setAnisotropy(sampler.getMaxAnisotropy());
        }

        return builder.createSamplerInfo();
    }
}