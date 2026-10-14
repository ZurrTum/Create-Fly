package com.zurrtum.create.client.flywheel.backend.vulkan.state;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;

import com.zurrtum.create.client.flywheel.api.material.DepthTest;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.material.WriteMask;
import com.zurrtum.create.client.flywheel.backend.Samplers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

/**
 * The Vulkan counterpart of
 * {@code com.zurrtum.create.client.flywheel.backend.engine.MaterialRenderState}.
 *
 * <p>Same six {@code setup*} steps, same order, same meaning — but driven
 * through {@link VkRenderState} instead of {@code GlStateManager}, so the state
 * lands on VulkanMod's pipeline shadow rather than on a GL context that does not
 * exist. {@code MaterialRenderState} is left untouched so the GL path keeps
 * working unchanged.
 *
 * <h2>Ordering contract</h2>
 *
 * <p><b>{@link #setup} must run before the pipeline is bound, not merely before
 * the draw.</b> VulkanMod snapshots state inside
 * {@code Renderer.bindGraphicsPipeline}, which is what Beryl's
 * {@code VBO.bind(pipeline)} and {@code BlitUtil.blitFramebuffer} both call, and
 * it is the step that issues {@code vkCmdBindPipeline}. Changing
 * {@code VRenderSystem} afterwards has no retroactive effect; the already-bound
 * pipeline keeps the old state and the draw silently uses it.
 *
 * <p>The ordering Flywheel currently uses is the wrong way round for Vulkan:
 *
 * <pre>{@code
 *   program.bind();                 // <- would bind with the PREVIOUS material's state
 *   environment.setupDraw(program);
 *   uploadMaterialUniform(program, material);
 *   MaterialRenderState.setup(material);
 *   drawCall.render(instanceTexture);
 * }</pre>
 *
 * <p>so the wiring needs to move the state call up:
 *
 * <pre>{@code
 *   VkMaterialState.setup(material);
 *   program.bind();
 *   environment.setupDraw(program);
 *   uploadMaterialUniform(program, material);
 *   drawCall.render(instanceTexture);
 * }</pre>
 *
 * <p>Note also that {@code Renderer.bindGraphicsPipeline} is where descriptors
 * are written, so {@link #setupTexture} must also precede the bind — the sampler
 * it attaches to the image is read at that moment.
 *
 * <h2>Repeating a state is free</h2>
 *
 * <p>Every setter here bottoms out in a field store. {@code bindGraphicsPipeline}
 * then compares the folded state against its cached {@code currentState} and
 * returns early when the resulting pipeline handle is already bound. So calling
 * {@code setup} once per draw, as Flywheel does today, costs a handful of field
 * stores and nothing else; no {@code VkPipeline} is rebuilt. Sorting materials
 * still matters, but only because it keeps the <em>sequence</em> of states short
 * — an A-B-A-B alternation emits four {@code vkCmdBindPipeline} calls where
 * A-A-B-B emits two. Each distinct state combination does create one
 * {@code VkPipeline}, cached permanently by {@code GraphicsPipeline.getHandle}.
 *
 * <h2>Framebuffer</h2>
 *
 * <p>{@link #setupFrameBuffer} is a no-op by design. Under GL, Flywheel binds an
 * FBO here; under VulkanMod the render target <em>is</em> the currently open
 * {@code RenderPass}, owned by whoever opened it, and there is nothing for a
 * draw manager to bind. The method exists so the call sites can be swapped
 * one-for-one without leaving holes.
 */
public final class VkMaterialState {

    /**
     * Slope-scale and constant factors Flywheel uses for polygon offset, taken
     * from {@code MaterialRenderState.setupPolygonOffset}.
     */
    private static final float POLYGON_OFFSET_FACTOR = -1.0F;
    private static final float POLYGON_OFFSET_UNITS = -10.0F;

    private VkMaterialState() {
    }

    /**
     * Applies every state a material carries, in the same order as
     * {@code MaterialRenderState.setup}.
     */
    public static void setup(Material material) {
        setupTexture(material);
        setupBackfaceCulling(material.backfaceCulling());
        setupPolygonOffset(material.polygonOffset());
        setupDepthTest(material.depthTest());
        setupTransparency(material.transparency());
        setupWriteMask(material.writeMask());
    }

    /**
     * Applies the state an ordered-independent-transparency draw needs.
     *
     * <p>Matches {@code MaterialRenderState.setupOit}: everything except the
     * write mask, which is replaced by a colour-mask-only variant. Depth writing
     * is left at whatever the material asked for, which the OIT passes rely on
     * to build their accumulation buffer.
     */
    public static void setupOit(Material material) {
        setupTexture(material);
        setupBackfaceCulling(material.backfaceCulling());
        setupPolygonOffset(material.polygonOffset());
        setupDepthTest(material.depthTest());

        WriteMask mask = material.writeMask();
        boolean writeColor = mask.color();
        VkRenderState.colorMask(writeColor, writeColor, writeColor, writeColor);
    }

    // ------------------------------------------------------------------
    // The six steps
    // ------------------------------------------------------------------

    /**
     * Binds the material's diffuse texture and gives it the right sampler.
     *
     * <p>Two jobs, split across two very different mechanisms:
     *
     * <ul>
     *   <li><b>Which texture</b> goes to the diffuse unit. Handled by
     *       {@code VRenderSystem.setShaderTexture}, which stores an MC
     *       {@code GpuTextureView}; VulkanMod resolves it to a
     *       {@code VulkanImage} while binding descriptors. There is no active
     *       texture unit to select, and no cube-map branch — the view's own type
     *       carries that information.</li>
     *   <li><b>How it is filtered</b> goes through {@code SamplerManager}, not
     *       {@code VRenderSystem}. This is the part worth understanding: a
     *       sampler is neither pipeline state nor a per-unit binding on the
     *       Vulkan side. {@code DescriptorSets} reads {@code image.getSampler()}
     *       as it writes the descriptor, so the sampler is a property of the
     *       image. There is nothing to set on {@code VRenderSystem} for it, which
     *       is precisely why {@code blur} cannot be expressed the way
     *       {@code filterMode} is in the GL path.</li>
     * </ul>
     *
     * <p>Because the sampler lives on the shared image, it is re-asserted on
     * every draw: two materials on one texture that disagree about {@code blur}
     * would otherwise leak into each other.
     *
     * <p>GL expresses "no mipmaps" by leaving the min filter unsuffixed while
     * pinning {@code GL_TEXTURE_BASE_LEVEL} / {@code GL_TEXTURE_MAX_LEVEL}.
     * Vulkan has no unsuffixed filter and no base level on the sampler, so the
     * equivalent is to clamp {@code maxLod} to the view's base level.
     */
    private static void setupTexture(Material material) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(material.texture());
        GpuTextureView view = texture.getTextureView();
        VkRenderState.setShaderTexture(Samplers.DIFFUSE.number, view);

        FilterMode filterMode = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
        int baseMipLevel = view.baseMipLevel();
        int maxLod = material.mipmap() ? baseMipLevel + view.mipLevels() - 1 : baseMipLevel;
        VkRenderState.setSampler(texture.getTexture(), texture.getSampler(), maxLod, filterMode);
    }

    private static void setupBackfaceCulling(boolean backfaceCulling) {
        if (backfaceCulling) {
            VkRenderState.enableCull();
        } else {
            VkRenderState.disableCull();
        }
    }

    private static void setupPolygonOffset(boolean polygonOffset) {
        if (polygonOffset) {
            VkRenderState.polygonOffset(POLYGON_OFFSET_FACTOR, POLYGON_OFFSET_UNITS);
            VkRenderState.enablePolygonOffset();
        } else {
            VkRenderState.polygonOffset(0.0F, 0.0F);
            VkRenderState.disablePolygonOffset();
        }
    }

    private static void setupDepthTest(DepthTest depthTest) {
        if (depthTest == DepthTest.OFF) {
            // No compare op: with the test off it is not part of the pipeline.
            VkRenderState.disableDepthTest();
            return;
        }

        VkRenderState.enableDepthTest();
        VkRenderState.depthFunc(VkDepthFunc.gl(depthTest));
    }

    /**
     * Transliterated from {@code MaterialRenderState.setupTransparency}. The
     * factor order is the GL one — see {@link VkRenderState#blendFuncSeparate},
     * which transposes it for VulkanMod on the way in.
     */
    private static void setupTransparency(Transparency transparency) {
        switch (transparency) {
            case OPAQUE -> VkRenderState.disableBlend();
            case ADDITIVE -> blend(VkBlendMode.ONE, VkBlendMode.ONE, VkBlendMode.ONE, VkBlendMode.ONE);
            case LIGHTNING -> blend(VkBlendMode.SRC_ALPHA, VkBlendMode.ONE, VkBlendMode.SRC_ALPHA, VkBlendMode.ONE);
            case GLINT -> blend(VkBlendMode.SRC_COLOR, VkBlendMode.ONE, VkBlendMode.ZERO, VkBlendMode.ONE);
            case CRUMBLING -> blend(VkBlendMode.DST_COLOR, VkBlendMode.SRC_COLOR, VkBlendMode.ONE, VkBlendMode.ZERO);
            case TRANSLUCENT -> blend(VkBlendMode.SRC_ALPHA, VkBlendMode.ONE_MINUS_SRC_ALPHA,
                    VkBlendMode.ONE, VkBlendMode.ONE_MINUS_SRC_ALPHA);
            // The GL switch simply has no case for this, leaving blend state
            // untouched. Honouring the enum's own contract — "a backend that
            // does not support OIT must treat this as TRANSLUCENT" — is both
            // closer to intent and safer, since inheriting a stale blend from
            // the previous material would be neither.
            case ORDER_INDEPENDENT -> blend(VkBlendMode.SRC_ALPHA, VkBlendMode.ONE_MINUS_SRC_ALPHA,
                    VkBlendMode.ONE, VkBlendMode.ONE_MINUS_SRC_ALPHA);
        }
    }

    private static void setupWriteMask(WriteMask mask) {
        VkRenderState.depthMask(mask.depth());
        boolean writeColor = mask.color();
        VkRenderState.colorMask(writeColor, writeColor, writeColor, writeColor);
    }

    // ------------------------------------------------------------------
    // Pass boundaries
    // ------------------------------------------------------------------

    /**
     * Restores the defaults Flywheel leaves behind at the end of a pass, matching
     * {@code MaterialRenderState.reset}.
     *
     * <p>There is no framebuffer or texture-unit reset: on Vulkan there is no
     * FBO binding to undo, and texture units are addressed by index rather than
     * by an "active unit" cursor.
     *
     * <p>Worth noting for anyone diffing this against
     * {@code MaterialRenderState.reset}: the values here are Flywheel's
     * post-pass defaults, not VulkanMod's. VulkanMod's own static defaults are
     * depth test <em>on</em> with {@code GL_LEQUAL} and culling on; Flywheel's
     * reset leaves the depth test <em>off</em>. This class mirrors Flywheel, so
     * that behaviour after a Flywheel pass is identical on both backends.
     */
    public static void reset() {
        resetBackfaceCulling();
        resetPolygonOffset();
        resetDepthTest();
        resetTransparency();
        resetWriteMask();
    }

    /**
     * No-op. See the class javadoc: under VulkanMod the render target is the
     * open {@code RenderPass}, which is not the draw manager's to bind.
     */
    public static void setupFrameBuffer() {
    }

    private static void resetBackfaceCulling() {
        VkRenderState.enableCull();
    }

    private static void resetPolygonOffset() {
        VkRenderState.polygonOffset(0.0F, 0.0F);
        VkRenderState.disablePolygonOffset();
    }

    private static void resetDepthTest() {
        VkRenderState.disableDepthTest();
        VkRenderState.depthFunc(VkDepthFunc.LEQUAL);
    }

    private static void resetTransparency() {
        VkRenderState.disableBlend();
        VkRenderState.blendFuncSeparate(VkBlendMode.SRC_ALPHA, VkBlendMode.ONE_MINUS_SRC_ALPHA, VkBlendMode.ONE, VkBlendMode.ZERO);
    }

    private static void resetWriteMask() {
        VkRenderState.depthMask(true);
        VkRenderState.colorMask(true, true, true, true);
    }

    private static void blend(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        VkRenderState.enableBlend();
        VkRenderState.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
    }
}