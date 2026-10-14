package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.gl.GlTextureUnit;

import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.PipelineConfig;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;

/**
 * The Vulkan counterpart of {@code com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram}.
 *
 * <p><b>This is not a drop-in replacement, and the differences are structural
 * rather than incidental.</b> OpenGL lets a live program be reconfigured after
 * linking — bind a uniform block to a point, point a sampler at a texture unit,
 * bind an attribute to a location. Vulkan bakes all three into the shader module
 * and the descriptor set layout <em>before</em> the pipeline exists. So:
 *
 * <ul>
 *   <li>Descriptor and attribute wiring moved to {@link Builder}, which runs before
 *       the pipeline is created. The GlProgram-shaped {@code setSamplerBinding} /
 *       {@code setUniformBlockBinding} / {@code bindAttribLocation} methods remain
 *       for call-site compatibility but are documented no-ops that warn once.</li>
 *   <li>{@code bind()} / {@code unbind()} have no meaning — there is no current
 *       program. Binding happens per-draw via {@link #handle(PipelineState)}.</li>
 *   <li>{@code getUniformLocation} does not exist. Use {@link #handle} and the
 *       uniform layer's own lookups.</li>
 * </ul>
 *
 * <p><b>Uniform writes</b> are forwarded to a {@link VkUniformAccess}, which
 * Agent C implements. Until one is attached every setter is a silent no-op; see
 * that interface for why that is the right failure mode.
 *
 * <p><b>Vertex attributes must already agree with the {@link VertexFormat}.</b>
 * VulkanMod derives attribute locations and formats from the format's element
 * order — {@code GraphicsPipeline.getAttributeDescriptions} assigns
 * {@code location(i)} positionally and maps a fixed set of (usage, type) pairs to
 * {@code VkFormat}s. There is no per-attribute binding hook to override it. The
 * GLSL declarations must therefore be written to match what that mapping
 * produces, which is a constraint on the shader sources, not on this class. See
 * {@link Builder#vertexFormat} for the specific mismatches in Flywheel's current
 * vertex layout.
 */
public final class VkProgram {

    private final GraphicsPipeline pipeline;
    private final String name;

    private VkUniformAccess uniforms;

    /**
     * Uniform names already reported as unknown, so the warning is logged once per
     * name rather than once per draw call.
     */
    private final Set<String> warnedUnknownUniforms = new LinkedHashSet<>();

    private VkProgram(GraphicsPipeline pipeline, String name) {
        this.pipeline = pipeline;
        this.name = name;
    }

    public String name() {
        return name;
    }

    /**
     * The underlying VulkanMod pipeline, for the draw code that has to bind
     * descriptor sets and issue the draw.
     */
    public GraphicsPipeline pipeline() {
        return pipeline;
    }

    /**
     * Attaches the uniform layer. Safe to call at any point before drawing;
     * setters called before this silently do nothing.
     */
    public void setUniformAccess(VkUniformAccess uniforms) {
        this.uniforms = uniforms;
    }

    /**
     * The {@code VkPipeline} handle for the given pipeline state, creating it on
     * first use.
     *
     * <p>VulkanMod caches one {@code VkPipeline} per state, because topology, cull
     * mode, blend, depth and colour mask are all baked into the pipeline. Flywheel
     * varies these per material, so this is called with a different state far more
     * often than Beryl calls it — but the caching is what makes that affordable.
     */
    public long handle(PipelineState state) {
        return pipeline.getHandle(state);
    }

    /**
     * The pipeline state matching whatever the current global render state is.
     *
     * <p>Pass {@code null} for the render pass; VulkanMod resolves the framebuffer
     * from its main pass under dynamic rendering, which is the configuration it
     * ships in.
     */
    public static PipelineState currentState() {
        return PipelineState.getCurrentPipelineState(null);
    }

    // ------------------------------------------------------------------
    // Uniform setters. Signature-compatible with GlProgram so that draw
    // code can be ported by changing the static type alone.
    // ------------------------------------------------------------------

    public void setFloat(String name, float value) {
        if (uniforms != null) {
            uniforms.setFloat(name, value);
        }
    }

    public void setVec2(String name, float x, float y) {
        if (uniforms != null) {
            uniforms.setVec2(name, x, y);
        }
    }

    public void setVec3(String name, float x, float y, float z) {
        if (uniforms != null) {
            uniforms.setVec3(name, x, y, z);
        }
    }

    public void setVec4(String name, float x, float y, float z, float w) {
        if (uniforms != null) {
            uniforms.setVec4(name, x, y, z, w);
        }
    }

    public void setMat4(String name, Matrix4fc matrix) {
        if (uniforms != null) {
            uniforms.setMat4(name, matrix);
        }
    }

    public void setMat3(String name, Matrix3fc matrix) {
        if (uniforms != null) {
            uniforms.setMat3(name, matrix);
        }
    }

    public void setInt(String name, int value) {
        if (uniforms != null) {
            uniforms.setInt(name, value);
        }
    }

    public void setUInt(String name, int value) {
        if (uniforms != null) {
            uniforms.setUInt(name, value);
        }
    }

    public void setBool(String name, boolean value) {
        setInt(name, value ? 1 : 0);
    }

    public void setUVec2(String name, int x, int y) {
        if (uniforms != null) {
            uniforms.setUVec2(name, x, y);
        }
    }

    // ------------------------------------------------------------------
    // Operations with no Vulkan equivalent. Kept so that porting draw code
    // does not require deleting call sites; each warns at most once.
    // ------------------------------------------------------------------

    /**
     * No-op. Vulkan has no current-program concept; see {@link #handle}.
     */
    public void bind() {
    }

    /**
     * No-op, for symmetry with {@link #bind()}.
     */
    public static void unbind() {
    }

    /**
     * Always returns {@code -1}.
     *
     * <p>GL uniform locations do not exist in Vulkan: the uniform layer resolves
     * members through the block's std140 layout instead. Callers that branch on the
     * return value will take the "uniform not active" path, which is the correct
     * behaviour for the uniforms this currently replaces.
     */
    public int getUniformLocation(String uniform) {
        return -1;
    }

    /**
     * No-op.
     *
     * <p>Sampler-to-texture wiring is a pipeline-creation-time decision in Vulkan.
     * Use {@link Builder#sampler(String, GlTextureUnit)} instead. Left in place so
     * that a ported call site fails quietly during bring-up rather than loudly
     * breaking the build.
     */
    public void setSamplerBinding(String name, GlTextureUnit binding) {
        warnOnce(name, "setSamplerBinding");
    }

    /**
     * @see #setSamplerBinding(String, GlTextureUnit)
     */
    public void setSamplerBinding(String name, int binding) {
        warnOnce(name, "setSamplerBinding");
    }

    /**
     * No-op.
     *
     * <p>Uniform block bindings are fixed when the descriptor set layout is built.
     * Use {@link Builder#uniformBlock(String, int, int)} instead, with the
     * corresponding binding in {@link VkBindings}.
     */
    public void setUniformBlockBinding(String name, int binding) {
        warnOnce(name, "setUniformBlockBinding");
    }

    /**
     * No-op.
     *
     * <p>Attribute locations come from the {@link VertexFormat}'s element order via
     * VulkanMod, not from a per-program binding. See {@link Builder#vertexFormat}.
     */
    public void bindAttribLocation(String attribute, int binding) {
        warnOnce(attribute, "bindAttribLocation");
    }

    private void warnOnce(String what, String operation) {
        if (warnedUnknownUniforms.add(what + "/" + operation)) {
            FlwBackend.LOGGER.warn(
                "[FW/VK] {}() on '{}' is a no-op under Vulkan; the real work happens at pipeline "
                    + "creation. Configure it on VkProgram.Builder instead.",
                operation, what
            );
        }
    }

    /**
     * Releases the pipeline and its descriptor sets.
     *
     * <p>Mirrors {@code GlObject#delete}, which the GL backend's
     * {@code CompilationHarness#delete} calls on every cached program.
     */
    public void delete() {
        pipeline.scheduleCleanUp();
    }

    /**
     * Collects everything VulkanMod needs to build a {@link GraphicsPipeline}, then
     * builds it.
     *
     * <p>Mirrors the GlProgram-shaped API so that {@code setUniformBlockBinding} /
     * {@code setSamplerBinding} / {@code bindAttribLocation} keep their call sites,
     * with the timing made explicit.
     */
    public static final class Builder {

        private final String name;
        private final VertexFormat vertexFormat;

        private final Map<String, Integer> uniformBlocks = new LinkedHashMap<>();
        private final Map<String, Integer> uniformBlockSizes = new LinkedHashMap<>();
        private final List<UBO> customUbos = new ArrayList<>();

        private final Map<String, Integer> samplerBindings = new LinkedHashMap<>();
        private final Map<String, Integer> samplerImageIdx = new LinkedHashMap<>();
        private final List<ImageDescriptor> customImageDescriptors = new ArrayList<>();

        private String vertexSource;
        private String fragmentSource;

        public Builder(String name, VertexFormat vertexFormat) {
            this.name = name;
            this.vertexFormat = vertexFormat;
        }

        /**
         * Pre-populates all six uniform blocks (bindings {@code 0..5}) and all seven
         * instancing samplers (bindings {@code 6..12}) defined in {@link VkBindings}.
         */
        public Builder withDefaultInstancingLayout() {
            for (var entry : VkBindings.blockBindings().entrySet()) {
                String blockName = entry.getKey();
                int binding = entry.getValue();
                uniformBlock(blockName, binding, VkBindings.blockSize(blockName));
            }
            for (var entry : VkBindings.instancingSamplerUnits().entrySet()) {
                sampler(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Directly adds a pre-built {@link UBO} descriptor (e.g. from Agent C's
         * uniform layer).
         */
        public Builder addUBO(UBO ubo) {
            this.customUbos.add(ubo);
            return this;
        }

        /**
         * Directly adds a pre-built {@link ImageDescriptor}.
         */
        public Builder addImageDescriptor(ImageDescriptor imageDescriptor) {
            this.customImageDescriptors.add(imageDescriptor);
            return this;
        }

        /**
         * Declares a uniform block backed by a manually sized descriptor.
         *
         * @param blockName  GLSL block name, e.g. {@code _FlwFrameUniforms}
         * @param binding    descriptor binding; must equal
         *                    {@link VkBindings#blockBinding(String)}
         * @param sizeBytes  the block's std140 size
         */
        public Builder uniformBlock(String blockName, int binding, int sizeBytes) {
            if (sizeBytes <= 0) {
                throw new IllegalArgumentException(
                    "Uniform block '" + blockName + "' has size " + sizeBytes + "; VulkanMod rejects non-positive sizes"
                );
            }

            int expected = VkBindings.blockBinding(blockName);
            if (expected >= 0 && expected != binding) {
                throw new IllegalArgumentException(
                    "Uniform block '" + blockName + "' bound to " + binding + " but VkBindings says "
                        + expected + ". The GLSL and the descriptor set layout must agree or "
                        + "pipeline creation fails."
                );
            }

            uniformBlocks.put(blockName, binding);
            uniformBlockSizes.put(blockName, sizeBytes);

            return this;
        }

        /**
         * Declares a sampler.
         */
        public Builder sampler(String samplerName, GlTextureUnit unit) {
            int binding = VkBindings.samplerBinding(samplerName);

            if (binding < 0) {
                throw new IllegalArgumentException(
                    "Sampler '" + samplerName + "' has no binding in VkBindings; add it or the "
                        + "descriptor set layout will not match the shader"
                );
            }

            samplerBindings.put(samplerName, binding);
            samplerImageIdx.put(samplerName, VkBindings.imageIdx(unit));

            return this;
        }

        /**
         * The already-assembled vertex shader text, post-Vulkan-rewrite.
         */
        public Builder vertexSource(String source) {
            this.vertexSource = source;
            return this;
        }

        /**
         * The already-assembled fragment shader text, post-Vulkan-rewrite.
         */
        public Builder fragmentSource(String source) {
            this.fragmentSource = source;
            return this;
        }

        /**
         * Creates the pipeline.
         *
         * <p>Runs shaderc via VulkanMod for both stages, then builds the descriptor
         * set layout, the pipeline layout and one descriptor set per frame. This is
         * the expensive step — callers are expected to cache the result, which is
         * what {@code VkProgramCache} does.
         *
         * @throws RuntimeException if shaderc rejects either stage, or if VulkanMod
         *                          cannot create the pipeline
         */
        public VkProgram build() {
            if (vertexSource == null || fragmentSource == null) {
                throw new IllegalStateException("Both vertex and fragment sources are required");
            }

            Pipeline.Builder builder = Pipeline.builder(vertexFormat, name);

            builder.setShaderSrc(SPIRVUtils.ShaderKind.VERTEX_SHADER, vertexSource);
            builder.setShaderSrc(SPIRVUtils.ShaderKind.FRAGMENT_SHADER, fragmentSource);

            for (UBO ubo : customUbos) {
                builder.addUBO(ubo);
            }

            for (var entry : uniformBlocks.entrySet()) {
                String blockName = entry.getKey();
                int binding = entry.getValue();

                builder.addUBO(new UBO(
                    blockName,
                    binding,
                    // "all" -> VK_SHADER_STAGE_ALL_GRAPHICS, via VulkanMod's own
                    // mapping so the constant cannot drift.
                    PipelineConfig.getStageFromString("all"),
                    uniformBlockSizes.get(blockName),
                    null
                ));
            }

            for (ImageDescriptor descriptor : customImageDescriptors) {
                builder.addImageDescriptor(descriptor);
            }

            for (var entry : samplerBindings.entrySet()) {
                String samplerName = entry.getKey();

                builder.addImageDescriptor(new ImageDescriptor(
                    entry.getValue(),
                    "sampler2D",
                    samplerName,
                    samplerImageIdx.get(samplerName),
                    VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                ));
            }

            FlwBackend.LOGGER.info(
                "[FW/VK] building pipeline '{}': {} uniform blocks, {} samplers",
                name,
                customUbos.size() + uniformBlocks.size(),
                customImageDescriptors.size() + samplerBindings.size()
            );

            return new VkProgram(builder.createGraphicsPipeline(), name);
        }

        /**
         * The samplers declared so far, for callers that need to mirror them into a
         * texture-binding table.
         */
        public List<String> samplerNames() {
            return new ArrayList<>(samplerBindings.keySet());
        }
    }
}