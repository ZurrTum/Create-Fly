package com.zurrtum.create.client.flywheel.backend.vulkan.state;

import static com.mojang.blaze3d.opengl.GlConst.GL_DST_ALPHA;
import static com.mojang.blaze3d.opengl.GlConst.GL_DST_COLOR;
import static com.mojang.blaze3d.opengl.GlConst.GL_FUNC_ADD;
import static com.mojang.blaze3d.opengl.GlConst.GL_FUNC_REVERSE_SUBTRACT;
import static com.mojang.blaze3d.opengl.GlConst.GL_FUNC_SUBTRACT;
import static com.mojang.blaze3d.opengl.GlConst.GL_MAX;
import static com.mojang.blaze3d.opengl.GlConst.GL_MIN;
import static com.mojang.blaze3d.opengl.GlConst.GL_ONE;
import static com.mojang.blaze3d.opengl.GlConst.GL_ONE_MINUS_DST_ALPHA;
import static com.mojang.blaze3d.opengl.GlConst.GL_ONE_MINUS_DST_COLOR;
import static com.mojang.blaze3d.opengl.GlConst.GL_ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.opengl.GlConst.GL_ONE_MINUS_SRC_COLOR;
import static com.mojang.blaze3d.opengl.GlConst.GL_SRC_ALPHA;
import static com.mojang.blaze3d.opengl.GlConst.GL_SRC_COLOR;
import static com.mojang.blaze3d.opengl.GlConst.GL_ZERO;

/**
 * Blend factors and equations in the GL encoding VulkanMod accepts, plus the
 * one argument-order trap that comes with them.
 *
 * <p><b>Factors are GL enums.</b> {@code PipelineState.BlendInfo.glToVulkanBlendFactor}
 * is a {@code lookupswitch} over the twelve classic GL factors — {@code 0},
 * {@code 1} and {@code 768..776} — and {@code glToVulkanBlendOp} a
 * {@code tableswitch} over {@code 32774..32779}. Out-of-range values throw.
 * Every factor {@code MaterialRenderState.setupTransparency} uses is in range,
 * including the two less common ones this backend relies on:
 * {@code GL_SRC_COLOR} (768) for glint and {@code GL_DST_COLOR} (774) for the
 * crumbling overlay.
 *
 * <p><b>Argument order is <em>not</em> the GL order.</b> This is the single
 * easiest thing to get wrong, so it is stated plainly:
 *
 * <pre>
 *   GL   glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
 *   VM   VRenderSystem.blendFuncSeparate(srcRGB, srcAlpha, dstRGB, dstAlpha)
 *                                                 ^^^^^^  ^^^^^^  swapped
 * </pre>
 *
 * Reading {@code BlendInfo.setBlendFuncSeparate}'s bytecode: local 1 is stored to
 * {@code srcRgbFactor}, local 3 to {@code dstRgbFactor}, local 2 to
 * {@code srcAlphaFactor}, local 4 to {@code dstAlphaFactor}. Positions two and
 * three are transposed relative to GL.
 *
 * <p>Beryl writes these calls in GL order
 * ({@code RenderingPipeline:876}, {@code BerylCloudRenderer:123}), so copying
 * Beryl's literals verbatim produces a subtly wrong blend. {@link
 * VkRenderState#blendFuncSeparate} takes GL-ordered arguments to match
 * {@code GlStateManager._blendFuncSeparate} exactly and does the permutation in
 * one place, so callers can transliterate the GL code line for line.
 *
 * <p>{@link VkRenderState#blendOp} is exposed but unused: Flywheel never changes
 * the blend equation, and VulkanMod's {@code BlendInfo.blendOp} default already
 * is {@code GL_FUNC_ADD}, which maps to {@code VK_BLEND_OP_ADD}.
 */
public final class VkBlendMode {

    // ---- factors (see PipelineState.BlendInfo.glToVulkanBlendFactor) ----

    public static final int ZERO = GL_ZERO;                       // 0
    public static final int ONE = GL_ONE;                         // 1
    public static final int SRC_COLOR = GL_SRC_COLOR;             // 768
    public static final int ONE_MINUS_SRC_COLOR = GL_ONE_MINUS_SRC_COLOR;     // 769
    public static final int SRC_ALPHA = GL_SRC_ALPHA;             // 770
    public static final int ONE_MINUS_SRC_ALPHA = GL_ONE_MINUS_SRC_ALPHA;     // 771
    public static final int DST_ALPHA = GL_DST_ALPHA;             // 772
    public static final int ONE_MINUS_DST_ALPHA = GL_ONE_MINUS_DST_ALPHA;     // 773
    public static final int DST_COLOR = GL_DST_COLOR;             // 774
    public static final int ONE_MINUS_DST_COLOR = GL_ONE_MINUS_DST_COLOR;     // 775
    /** The one factor {@code GlConst} does not define; Flywheel never uses it. */
    public static final int SRC_ALPHA_SATURATE = 776;

    // ---- equations (see PipelineState.BlendInfo.glToVulkanBlendOp) ----

    public static final int ADD = GL_FUNC_ADD;                    // 32774 -> VK_BLEND_OP_ADD
    public static final int MIN = GL_MIN;                         // 32775 -> VK_BLEND_OP_MIN
    public static final int MAX = GL_MAX;                         // 32776 -> VK_BLEND_OP_MAX
    public static final int SUBTRACT = GL_FUNC_SUBTRACT;          // 32778 -> VK_BLEND_OP_SUBTRACT
    public static final int REVERSE_SUBTRACT = GL_FUNC_REVERSE_SUBTRACT;  // 32779 -> VK_BLEND_OP_REVERSE_SUBTRACT

    private VkBlendMode() {
    }
}