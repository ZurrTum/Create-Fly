package com.zurrtum.create.client.flywheel.backend.vulkan.state;

import com.zurrtum.create.client.flywheel.api.material.DepthTest;

import org.lwjgl.opengl.GL11;

/**
 * Depth comparison functions in the encoding VulkanMod expects.
 *
 * <p><b>The compare op is passed as a GL enum, not a {@code VkCompareOp}.</b>
 * {@code VRenderSystem} exposes both a {@code depthFunc(int)} setter and a
 * {@code public static int depthFun} field; both simply store whatever int they
 * are given. The translation happens later, in
 * {@code PipelineState.DepthState.glToVulkan(int)}, which is a {@code tableswitch}
 * over exactly {@code 512..519}:
 *
 * <table border="1">
 *   <caption>VulkanMod depth func translation</caption>
 *   <tr><th>GL constant</th><th>value</th><th>{@code VkCompareOp}</th><th>ordinal</th></tr>
 *   <tr><td>{@code GL_NEVER}</td><td>512</td><td>{@code NEVER}</td><td>0</td></tr>
 *   <tr><td>{@code GL_LESS}</td><td>513</td><td>{@code LESS}</td><td>1</td></tr>
 *   <tr><td>{@code GL_EQUAL}</td><td>514</td><td>{@code EQUAL}</td><td>2</td></tr>
 *   <tr><td>{@code GL_LEQUAL}</td><td>515</td><td>{@code LESS_OR_EQUAL}</td><td>3</td></tr>
 *   <tr><td>{@code GL_GREATER}</td><td>516</td><td>{@code GREATER}</td><td>4</td></tr>
 *   <tr><td>{@code GL_NOTEQUAL}</td><td>517</td><td>{@code NOT_EQUAL}</td><td>5</td></tr>
 *   <tr><td>{@code GL_GEQUAL}</td><td>518</td><td>{@code GREATER_OR_EQUAL}</td><td>6</td></tr>
 *   <tr><td>{@code GL_ALWAYS}</td><td>519</td><td>{@code ALWAYS}</td><td>7</td></tr>
 * </table>
 *
 * <p>Anything outside that range throws
 * {@code RuntimeException("unknown blend factor: %d")} — and it throws
 * <em>late</em>, at {@code getCurrentPipelineState} time when a draw is bound,
 * not when {@code depthFunc} is called. A bad value therefore surfaces as a
 * crash in the middle of a frame rather than at the offending call site.
 *
 * <p>All eight comparisons Flywheel's {@link DepthTest} can ask for are covered.
 * {@link DepthTest#OFF} is not a comparison at all: it disables the test and
 * leaves the compare op unused, which is why it maps to {@link #NONE} rather
 * than to a real constant.
 *
 * <p>Every constant here is a {@code static final int} compile-time constant, so
 * referencing {@code GL11} costs nothing at runtime and no OpenGL context is
 * ever touched.
 */
public final class VkDepthFunc {

    /**
     * Sentinel for {@link DepthTest#OFF}. Never passed to VulkanMod — when the
     * depth test is disabled the compare op is not part of the pipeline state.
     */
    public static final int NONE = -1;

    public static final int NEVER = GL11.GL_NEVER;
    public static final int LESS = GL11.GL_LESS;
    public static final int EQUAL = GL11.GL_EQUAL;
    public static final int LEQUAL = GL11.GL_LEQUAL;
    public static final int GREATER = GL11.GL_GREATER;
    public static final int NOTEQUAL = GL11.GL_NOTEQUAL;
    public static final int GEQUAL = GL11.GL_GEQUAL;
    public static final int ALWAYS = GL11.GL_ALWAYS;

    private VkDepthFunc() {
    }

    /**
     * Maps a Flywheel depth test onto the GL encoding VulkanMod wants.
     *
     * @return {@link #NONE} for {@link DepthTest#OFF}, otherwise the GL compare
     *         enum that {@code PipelineState.DepthState} will translate.
     */
    public static int gl(DepthTest depthTest) {
        return switch (depthTest) {
            case OFF -> NONE;
            case NEVER -> NEVER;
            case LESS -> LESS;
            case EQUAL -> EQUAL;
            case LEQUAL -> LEQUAL;
            case GREATER -> GREATER;
            case NOTEQUAL -> NOTEQUAL;
            case GEQUAL -> GEQUAL;
            case ALWAYS -> ALWAYS;
        };
    }
}