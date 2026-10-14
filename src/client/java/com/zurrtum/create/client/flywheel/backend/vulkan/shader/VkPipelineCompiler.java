package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.backend.BackendConfig;
import com.zurrtum.create.client.flywheel.backend.InternalVertex;
import com.zurrtum.create.client.flywheel.backend.MaterialShaderIndices;
import com.zurrtum.create.client.flywheel.backend.compile.ContextShader;
import com.zurrtum.create.client.flywheel.backend.compile.FlwPrograms;
import com.zurrtum.create.client.flywheel.backend.compile.LightSmoothness;
import com.zurrtum.create.client.flywheel.backend.compile.Pipeline;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler.OitMode;
import com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler.PipelineProgramKey;
import com.zurrtum.create.client.flywheel.backend.compile.Pipelines;
import com.zurrtum.create.client.flywheel.backend.compile.component.InstanceStructComponent;
import com.zurrtum.create.client.flywheel.backend.compile.component.UberShaderComponent;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.FrameUniforms;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.FnSignature;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.GlslExpr;
import com.zurrtum.create.client.flywheel.lib.material.CutoutShaders;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Vulkan counterpart of {@code com.zurrtum.create.client.flywheel.backend.compile.PipelineCompiler}.
 *
 * <p>Replicates the shader assembly chain from {@code PipelineCompiler.create()}
 * (lines 84-179) inside {@code backend/vulkan/shader} because the original
 * {@code Compile} / {@code CompilationHarness} / {@code ShaderCache} /
 * {@code ProgramLinker} chain hardcodes {@code GlProgram} and {@code GlShader}
 * return types and lives in the read-only {@code backend/compile} package.
 *
 * <p>Key differences from the GL {@code PipelineCompiler}:
 * <ul>
 *   <li>Always emits {@code #version 450} via {@link VkShaderSource}.</li>
 *   <li>Never emits {@code GL_ARB_conservative_depth},
 *       {@code GL_ARB_shader_bit_encoding}, or the fallback {@code fma(a,b,c)}
 *       macro.</li>
 *   <li>Forces {@link OitMode#OFF} in v1 so {@code common.frag} always takes the
 *       single-output {@code out vec4 _flw_outputColor;} branch.</li>
 *   <li>Normalises declarations for Vulkan (Option C {@code sampler2D} buffer
 *       lookup rewrite, {@code _FlwDrawUniforms} UBO wrapping, stage
 *       {@code layout(location = N)}, and {@code layout(binding = N)}) before
 *       passing the source through {@link VkShaderCompiler} and
 *       {@code VkGLSLAssembler}.</li>
 * </ul>
 */
public final class VkPipelineCompiler {

    private static final Identifier API_IMPL_VERT = ResourceUtil.rl("internal/api_impl.vert");
    private static final Identifier API_IMPL_FRAG = ResourceUtil.rl("internal/api_impl.frag");
    private static final Identifier COMPONENTS_HEADER_FRAG = ResourceUtil.rl("internal/components_header.frag");

    private final ShaderSources sources;
    private final Pipeline pipeline;
    private final VertexFormat vertexFormat;
    private final Consumer<VkProgram.Builder> builderCustomizer;
    private final VkProgramCache<PipelineProgramKey> cache = new VkProgramCache<>();

    @Nullable
    private VkUniformAccess uniformAccess;

    @Nullable
    private UberShaderComponent fogComponent;
    @Nullable
    private UberShaderComponent cutoutComponent;
    private int lastFogCount = -1;
    private int lastCutoutCount = -1;

    public VkPipelineCompiler(
        ShaderSources sources,
        Pipeline pipeline,
        VertexFormat vertexFormat,
        Consumer<VkProgram.Builder> builderCustomizer
    ) {
        this.sources = sources;
        this.pipeline = pipeline;
        this.vertexFormat = vertexFormat;
        this.builderCustomizer = builderCustomizer;
    }

    /**
     * Creates an instancing pipeline compiler backed by {@link FlwPrograms#SOURCES}
     * and the default descriptor layout from {@link VkBindings}.
     */
    public static VkPipelineCompiler createInstancing(VertexFormat vertexFormat) {
        return new VkPipelineCompiler(
            FlwPrograms.SOURCES,
            Pipelines.INSTANCING,
            vertexFormat,
            VkProgram.Builder::withDefaultInstancingLayout
        );
    }

    /**
     * Convenience overload using {@link DefaultVertexFormat#EMPTY} when vertex
     * inputs are bound or supplied externally.
     */
    public static VkPipelineCompiler createInstancing() {
        return createInstancing(DefaultVertexFormat.EMPTY);
    }

    /**
     * Attaches the uniform accessor (implemented by Agent C) that every compiled
     * {@link VkProgram} will forward its {@code setXxx} calls to.
     */
    public VkPipelineCompiler setUniformAccess(@Nullable VkUniformAccess uniformAccess) {
        this.uniformAccess = uniformAccess;
        return this;
    }

    /**
     * Returns the compiled {@link VkProgram} for the given draw parameters,
     * compiling and caching it on first use.
     *
     * <p>Note: {@code oit} is accepted for signature parity with
     * {@code InstancingPrograms#get}, but v1 always forces {@link OitMode#OFF}.
     */
    public VkProgram get(
        InstanceType<?> instanceType,
        ContextShader contextShader,
        Material material,
        OitMode oit
    ) {
        var light = material.light();
        var cutout = material.cutout();
        var shaders = material.shaders();
        var fog = material.fog();

        MaterialShaderIndices.fogSources().index(fog.source());
        MaterialShaderIndices.cutoutSources().index(cutout.source());
        ensureUberComponentsUpToDate();

        PipelineProgramKey key = new PipelineProgramKey(
            instanceType,
            contextShader,
            light,
            shaders,
            cutout != CutoutShaders.OFF,
            FrameUniforms.debugOn(),
            OitMode.OFF
        );

        VkProgram program = cache.get(key, this::compileProgram);
        if (uniformAccess != null) {
            program.setUniformAccess(uniformAccess);
        }
        return program;
    }

    private void ensureUberComponentsUpToDate() {
        int fogCount = MaterialShaderIndices.fogSources().all().size();
        int cutoutCount = MaterialShaderIndices.cutoutSources().all().size();

        if (fogComponent != null && cutoutComponent != null
            && fogCount == lastFogCount && cutoutCount == lastCutoutCount) {
            return;
        }

        if (lastFogCount != -1 || lastCutoutCount != -1) {
            cache.delete();
        }

        fogComponent = UberShaderComponent.builder(ResourceUtil.rl("fog"))
            .materialSources(MaterialShaderIndices.fogSources().all())
            .adapt(
                FnSignature.create().returnType("vec4").name("flw_fogFilter").arg("vec4", "color").build(),
                GlslExpr.variable("color")
            )
            .switchOn(GlslExpr.variable("_flw_uberFogIndex"))
            .build(sources);

        cutoutComponent = UberShaderComponent.builder(ResourceUtil.rl("cutout"))
            .materialSources(MaterialShaderIndices.cutoutSources().all())
            .adapt(
                FnSignature.create().returnType("bool").name("flw_discardPredicate").arg("vec4", "color").build(),
                GlslExpr.boolLiteral(false)
            )
            .switchOn(GlslExpr.variable("_flw_uberCutoutIndex"))
            .build(sources);

        lastFogCount = fogCount;
        lastCutoutCount = cutoutCount;
    }

    private VkProgram compileProgram(PipelineProgramKey key) {
        String vertName = vertexShaderName(key);
        String fragName = fragmentShaderName(key);

        String vertGlsl = cache.prepare(vertName, VkShaderStage.VERTEX, assembleVertexSource(key));
        String fragGlsl = cache.prepare(fragName, VkShaderStage.FRAGMENT, assembleFragmentSource(key));

        VkProgram.Builder builder = new VkProgram.Builder(vertName, vertexFormat)
            .vertexSource(vertGlsl)
            .fragmentSource(fragGlsl);

        builderCustomizer.accept(builder);

        VkProgram program = builder.build();
        if (uniformAccess != null) {
            program.setUniformAccess(uniformAccess);
        }
        return program;
    }

    private String assembleVertexSource(PipelineProgramKey key) {
        VkShaderSource ctx = new VkShaderSource();
        ctx.version();
        ctx.stagePreamble(VkShaderStage.VERTEX);

        applyContextShaderDefines(key.contextShader(), ctx);
        applyLightSmoothnessDefines(BackendConfig.INSTANCE.lightSmoothness(), ctx);

        if (key.debugEnabled()) {
            ctx.define("_FLW_DEBUG");
        }

        List<SourceComponent> roots = new ArrayList<>();
        roots.add(sources.get(API_IMPL_VERT));
        roots.add(new InstanceStructComponent(key.instanceType()));
        roots.add(sources.get(key.instanceType().vertexShader()));
        roots.add(sources.get(key.materialShaders().vertexSource()));
        roots.add(sources.get(InternalVertex.LAYOUT_SHADER_VK));
        roots.add(pipeline.assembler().assemble(key.instanceType()));
        roots.add(sources.get(pipeline.vertexMain()));

        for (SourceComponent component : VkShaderSource.expand(roots)) {
            ctx.appendComponent(component);
        }

        ctx.prepareForVulkan(VkShaderStage.VERTEX);
        return ctx.build();
    }

    private String assembleFragmentSource(PipelineProgramKey key) {
        VkShaderSource ctx = new VkShaderSource();
        ctx.version();
        ctx.stagePreamble(VkShaderStage.FRAGMENT);

        applyContextShaderDefines(key.contextShader(), ctx);
        applyLightSmoothnessDefines(BackendConfig.INSTANCE.lightSmoothness(), ctx);

        if (key.debugEnabled()) {
            ctx.define("_FLW_DEBUG");
        }
        if (key.useCutout()) {
            ctx.define("_FLW_USE_DISCARD");
        }
        if (key.oit() != OitMode.OFF) {
            ctx.define("_FLW_OIT");
            ctx.define(key.oit().define);
        }

        List<SourceComponent> roots = new ArrayList<>();
        roots.add(sources.get(API_IMPL_FRAG));
        roots.add(sources.get(key.materialShaders().fragmentSource()));
        roots.add(sources.get(COMPONENTS_HEADER_FRAG));
        roots.add(fogComponent);
        roots.add(sources.get(key.light().source()));
        roots.add(key.useCutout() ? cutoutComponent : sources.get(CutoutShaders.OFF.source()));
        roots.add(sources.get(pipeline.fragmentMain()));

        for (SourceComponent component : VkShaderSource.expand(roots)) {
            ctx.appendComponent(component);
        }

        ctx.prepareForVulkan(VkShaderStage.FRAGMENT);
        return ctx.build();
    }

    private static void applyContextShaderDefines(ContextShader contextShader, VkShaderSource ctx) {
        switch (contextShader) {
            case DEFAULT -> {
            }
            case CRUMBLING -> ctx.define("_FLW_CRUMBLING");
            case EMBEDDED -> ctx.define("FLW_EMBEDDED");
        }
    }

    private static void applyLightSmoothnessDefines(LightSmoothness smoothness, VkShaderSource ctx) {
        switch (smoothness) {
            case FLAT -> ctx.define("_FLW_LIGHT_SMOOTHNESS", 0);
            case TRI_LINEAR -> ctx.define("_FLW_LIGHT_SMOOTHNESS", 1);
            case SMOOTH -> ctx.define("_FLW_LIGHT_SMOOTHNESS", 2);
            case SMOOTH_INNER_FACE_CORRECTED -> {
                ctx.define("_FLW_LIGHT_SMOOTHNESS", 2);
                ctx.define("_FLW_INNER_FACE_CORRECTION");
            }
        }
    }

    private String vertexShaderName(PipelineProgramKey key) {
        var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType().vertexShader());
        var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders().vertexSource());
        var context = key.contextShader().nameLowerCase();
        var debug = key.debugEnabled() ? "_debug" : "";
        return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "/" + material + "_" + context + debug;
    }

    private String fragmentShaderName(PipelineProgramKey key) {
        var context = key.contextShader().nameLowerCase();
        var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders().fragmentSource());
        var light = ResourceUtil.toDebugFileNameNoExtension(key.light().source());
        var debug = key.debugEnabled() ? "_debug" : "";
        var cutout = key.useCutout() ? "_cutout" : "";
        var oit = key.oit().name;
        return "pipeline/" + pipeline.compilerMarker() + "/frag/" + material + "/" + light + "_" + context + cutout + debug + oit;
    }

    public void delete() {
        cache.delete();
    }
}