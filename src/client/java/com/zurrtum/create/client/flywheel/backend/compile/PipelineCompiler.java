package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.LightShader;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.MaterialShaders;
import com.zurrtum.create.client.flywheel.backend.BackendConfig;
import com.zurrtum.create.client.flywheel.backend.InternalVertex;
import com.zurrtum.create.client.flywheel.backend.MaterialShaderIndices;
import com.zurrtum.create.client.flywheel.backend.Samplers;
import com.zurrtum.create.client.flywheel.backend.compile.component.InstanceStructComponent;
import com.zurrtum.create.client.flywheel.backend.compile.component.UberShaderComponent;
import com.zurrtum.create.client.flywheel.backend.compile.core.CompilationHarness;
import com.zurrtum.create.client.flywheel.backend.compile.core.Compile;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.FrameUniforms;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.gl.shader.ShaderType;
import com.zurrtum.create.client.flywheel.backend.glsl.GlslVersion;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.FnSignature;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.GlslExpr;
import com.zurrtum.create.client.flywheel.lib.material.CutoutShaders;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.util.Identifier;

import java.util.*;

public final class PipelineCompiler {
    private static final Set<PipelineCompiler> ALL = Collections.newSetFromMap(new WeakHashMap<>());

    private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

    private static UberShaderComponent FOG;
    private static UberShaderComponent CUTOUT;

    private static final Identifier API_IMPL_VERT = ResourceUtil.rl("internal/api_impl.vert");
    private static final Identifier API_IMPL_FRAG = ResourceUtil.rl("internal/api_impl.frag");

    private final CompilationHarness<PipelineProgramKey> harness;

    public PipelineCompiler(CompilationHarness<PipelineProgramKey> harness) {
        this.harness = harness;
        ALL.add(this);
    }

    public GlProgram get(InstanceType<?> instanceType, ContextShader contextShader, Material material, OitMode oit) {
        var light = material.light();
        var cutout = material.cutout();
        var shaders = material.shaders();
        var fog = material.fog();

        // Tell fogSources to index the fog shader if we haven't seen it before.
        // If it is new, this will trigger a deletion of all programs.
        MaterialShaderIndices.fogSources().index(fog.source());

        // Same thing for cutout.
        // Add OFF to the index here anyway to ensure MaterialEncoder doesn't deleteAll at an inappropriate time.
        MaterialShaderIndices.cutoutSources().index(cutout.source());

        return harness.get(new PipelineProgramKey(
            instanceType,
            contextShader,
            light,
            shaders,
            cutout != CutoutShaders.OFF,
            FrameUniforms.debugOn(),
            oit
        ));
    }

    public void delete() {
        harness.delete();
    }

    public static void deleteAll() {
        createFogComponent();
        createCutoutComponent();
        ALL.forEach(PipelineCompiler::delete);
    }

    static PipelineCompiler create(
        ShaderSources sources,
        Pipeline pipeline,
        List<SourceComponent> vertexComponents,
        List<SourceComponent> fragmentComponents,
        Collection<String> extensions
    ) {
        // We could technically compile every version of light smoothness ahead of time,
        // but that seems unnecessary as I doubt most folks will be changing this option often.
        var harness = PIPELINE.program().link(PIPELINE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.VERTEX).nameMapper(key -> {
                    var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType().vertexShader());

                    var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders().vertexSource());
                    var context = key.contextShader().nameLowerCase();
                    var debug = key.debugEnabled() ? "_debug" : "";
                    return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "/" + material + "_" + context + debug;
                }).requireExtensions(extensions).onCompile((rl, compilation) -> {
                    if (GlCompat.MAX_GLSL_VERSION.compareTo(GlslVersion.V400) < 0 && !extensions.contains("GL_ARB_gpu_shader5")) {
                        // Only define fma if it wouldn't be declared by gpu shader 5
                        compilation.define("fma(a, b, c) ((a) * (b) + (c))");
                    }
                }).onCompile((key, comp) -> key.contextShader().onCompile(comp))
                .onCompile((key, comp) -> BackendConfig.INSTANCE.lightSmoothness().onCompile(comp)).onCompile((key, comp) -> {
                    if (key.debugEnabled()) {
                        comp.define("_FLW_DEBUG");
                    }
                }).withResource(API_IMPL_VERT).withComponent(key -> new InstanceStructComponent(key.instanceType()))
                .withResource(key -> key.instanceType().vertexShader()).withResource(key -> key.materialShaders().vertexSource())
                .withComponents(vertexComponents).withResource(InternalVertex.LAYOUT_SHADER)
                .withComponent(key -> pipeline.assembler().assemble(key.instanceType())).withResource(pipeline.vertexMain()))
            .link(PIPELINE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.FRAGMENT).nameMapper(key -> {
                    var context = key.contextShader().nameLowerCase();

                    var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders().fragmentSource());

                    var light = ResourceUtil.toDebugFileNameNoExtension(key.light().source());
                    var debug = key.debugEnabled() ? "_debug" : "";
                    var cutout = key.useCutout() ? "_cutout" : "";
                    var oit = key.oit().name;
                    return "pipeline/" + pipeline.compilerMarker() + "/frag/" + material + "/" + light + "_" + context + cutout + debug + oit;
                }).requireExtensions(extensions).enableExtension("GL_ARB_conservative_depth").onCompile((rl, compilation) -> {
                    if (GlCompat.MAX_GLSL_VERSION.compareTo(GlslVersion.V400) < 0 && !extensions.contains("GL_ARB_gpu_shader5")) {
                        // Only define fma if it wouldn't be declared by gpu shader 5
                        compilation.define("fma(a, b, c) ((a) * (b) + (c))");
                    }
                }).onCompile((key, comp) -> key.contextShader().onCompile(comp))
                .onCompile((key, comp) -> BackendConfig.INSTANCE.lightSmoothness().onCompile(comp)).onCompile((key, comp) -> {
                    if (key.debugEnabled()) {
                        comp.define("_FLW_DEBUG");
                    }
                }).onCompile((key, comp) -> {
                    if (key.useCutout()) {
                        comp.define("_FLW_USE_DISCARD");
                    }
                }).onCompile((key, comp) -> {
                    if (key.oit() != OitMode.OFF) {
                        comp.define("_FLW_OIT");
                        comp.define(key.oit().define);
                    }
                }).withResource(API_IMPL_FRAG).withResource(key -> key.materialShaders().fragmentSource()).withComponents(fragmentComponents)
                .withComponent(key -> FOG).withResource(key -> key.light().source())
                .with((key, fetcher) -> (key.useCutout() ? CUTOUT : fetcher.get(CutoutShaders.OFF.source()))).withResource(pipeline.fragmentMain()))
            .preLink((key, program) -> {
                program.bindAttribLocation("_flw_aPos", 0);
                program.bindAttribLocation("_flw_aColor", 1);
                program.bindAttribLocation("_flw_aTexCoord", 2);
                program.bindAttribLocation("_flw_aOverlay", 3);
                program.bindAttribLocation("_flw_aLight", 4);
                program.bindAttribLocation("_flw_aNormal", 5);
            }).postLink((key, program) -> {
                Uniforms.setUniformBlockBindings(program);

                program.bind();

                program.setSamplerBinding("flw_diffuseTex", Samplers.DIFFUSE);
                program.setSamplerBinding("flw_overlayTex", Samplers.OVERLAY);
                program.setSamplerBinding("flw_lightTex", Samplers.LIGHT);
                program.setSamplerBinding("_flw_depthRange", Samplers.DEPTH_RANGE);
                program.setSamplerBinding("_flw_coefficients", Samplers.COEFFICIENTS);
                program.setSamplerBinding("_flw_blueNoise", Samplers.NOISE);
                pipeline.onLink().accept(program);
                key.contextShader().onLink(program);

                GlProgram.unbind();
            }).harness(pipeline.compilerMarker(), sources);

        return new PipelineCompiler(harness);
    }

    public static void createFogComponent() {
        FOG = UberShaderComponent.builder(ResourceUtil.rl("fog")).materialSources(MaterialShaderIndices.fogSources().all())
            .adapt(FnSignature.create().returnType("vec4").name("flw_fogFilter").arg("vec4", "color").build(), GlslExpr.variable("color"))
            .switchOn(GlslExpr.variable("_flw_uberFogIndex")).build(FlwPrograms.SOURCES);
    }

    private static void createCutoutComponent() {
        CUTOUT = UberShaderComponent.builder(ResourceUtil.rl("cutout")).materialSources(MaterialShaderIndices.cutoutSources().all())
            .adapt(FnSignature.create().returnType("bool").name("flw_discardPredicate").arg("vec4", "color").build(), GlslExpr.boolLiteral(false))
            .switchOn(GlslExpr.variable("_flw_uberCutoutIndex")).build(FlwPrograms.SOURCES);
    }

    /**
     * Represents the entire context of a program's usage.
     *
     * @param instanceType  The instance shader to use.
     * @param contextShader The context shader to use.
     * @param light         The light shader to use.
     */
    public record PipelineProgramKey(
        InstanceType<?> instanceType, ContextShader contextShader, LightShader light, MaterialShaders materialShaders, boolean useCutout,
        boolean debugEnabled, OitMode oit
    ) {
    }

    public enum OitMode {
        OFF("", ""),
        DEPTH_RANGE("_FLW_DEPTH_RANGE", "_depth_range"),
        GENERATE_COEFFICIENTS("_FLW_COLLECT_COEFFS", "_generate_coefficients"),
        EVALUATE("_FLW_EVALUATE", "_resolve"),
        ;

        public final String define;
        public final String name;

        OitMode(String define, String name) {
            this.define = define;
            this.name = name;
        }
    }
}
