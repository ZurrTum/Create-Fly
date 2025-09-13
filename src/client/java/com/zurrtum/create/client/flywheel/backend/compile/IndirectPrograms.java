package com.zurrtum.create.client.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.backend.compile.component.InstanceStructComponent;
import com.zurrtum.create.client.flywheel.backend.compile.component.SsboInstanceComponent;
import com.zurrtum.create.client.flywheel.backend.compile.core.CompilationHarness;
import com.zurrtum.create.client.flywheel.backend.compile.core.Compile;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.gl.shader.ShaderType;
import com.zurrtum.create.client.flywheel.backend.glsl.GlslVersion;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.lib.util.AtomicReferenceCounted;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IndirectPrograms extends AtomicReferenceCounted {
    private static final Identifier CULL_SHADER_API_IMPL = ResourceUtil.rl("internal/indirect/cull_api_impl.glsl");
    private static final Identifier CULL_SHADER_MAIN = ResourceUtil.rl("internal/indirect/cull.glsl");
    private static final Identifier APPLY_SHADER_MAIN = ResourceUtil.rl("internal/indirect/apply.glsl");
    private static final Identifier SCATTER_SHADER_MAIN = ResourceUtil.rl("internal/indirect/scatter.glsl");
    private static final Identifier DOWNSAMPLE_FIRST = ResourceUtil.rl("internal/indirect/downsample_first.glsl");
    private static final Identifier DOWNSAMPLE_SECOND = ResourceUtil.rl("internal/indirect/downsample_second.glsl");

    private static final Compile<InstanceType<?>> CULL = new Compile<>();
    private static final Compile<Identifier> UTIL = new Compile<>();

    private static final List<String> EXTENSIONS = getExtensions(GlCompat.MAX_GLSL_VERSION);
    private static final List<String> COMPUTE_EXTENSIONS = getComputeExtensions(GlCompat.MAX_GLSL_VERSION);

    @Nullable
    private static IndirectPrograms instance;

    private final PipelineCompiler pipeline;
    private final CompilationHarness<InstanceType<?>> culling;
    private final CompilationHarness<Identifier> utils;
    private final OitPrograms oitPrograms;

    private IndirectPrograms(
        PipelineCompiler pipeline,
        CompilationHarness<InstanceType<?>> culling,
        CompilationHarness<Identifier> utils,
        OitPrograms oitPrograms
    ) {
        this.pipeline = pipeline;
        this.culling = culling;
        this.utils = utils;
        this.oitPrograms = oitPrograms;
    }

    private static List<String> getExtensions(GlslVersion glslVersion) {
        var extensions = ImmutableList.<String>builder();
        if (glslVersion.compareTo(GlslVersion.V400) < 0) {
            extensions.add("GL_ARB_gpu_shader5");
        }
        if (glslVersion.compareTo(GlslVersion.V420) < 0) {
            extensions.add("GL_ARB_shading_language_420pack");
            extensions.add("GL_ARB_shader_image_load_store");
        }
        if (glslVersion.compareTo(GlslVersion.V430) < 0) {
            extensions.add("GL_ARB_shader_storage_buffer_object");
            extensions.add("GL_ARB_shader_image_size");
        }
        if (glslVersion.compareTo(GlslVersion.V460) < 0) {
            extensions.add("GL_ARB_shader_draw_parameters");
        }
        return extensions.build();
    }

    private static List<String> getComputeExtensions(GlslVersion glslVersion) {
        var extensions = ImmutableList.<String>builder();

        extensions.addAll(EXTENSIONS);

        if (glslVersion.compareTo(GlslVersion.V430) < 0) {
            extensions.add("GL_ARB_compute_shader");
        }
        return extensions.build();
    }

    static void reload(ShaderSources sources, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
        if (!GlCompat.SUPPORTS_INDIRECT) {
            return;
        }

        var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, vertexComponents, fragmentComponents, EXTENSIONS);
        var cullingCompiler = createCullingCompiler(sources);
        var utilCompiler = createUtilCompiler(sources);
        var fullscreenCompiler = OitPrograms.createFullscreenCompiler(sources);

        IndirectPrograms newInstance = new IndirectPrograms(pipelineCompiler, cullingCompiler, utilCompiler, fullscreenCompiler);

        setInstance(newInstance);
    }

    /**
     * A compiler for cull shaders, parameterized by the instance type.
     */
    private static CompilationHarness<InstanceType<?>> createCullingCompiler(ShaderSources sources) {
        return CULL.program().link(CULL.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.COMPUTE)
            .nameMapper(instanceType -> "culling/" + ResourceUtil.toDebugFileNameNoExtension(instanceType.cullShader()))
            .requireExtensions(COMPUTE_EXTENSIONS).define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE).withResource(CULL_SHADER_API_IMPL)
            .withComponent(InstanceStructComponent::new).withResource(InstanceType::cullShader).withComponent(SsboInstanceComponent::new)
            .withResource(CULL_SHADER_MAIN)).postLink((key, program) -> Uniforms.setUniformBlockBindings(program)).harness("culling", sources);
    }

    /**
     * A compiler for utility shaders, directly compiles the shader at the resource location specified by the parameter.
     */
    private static CompilationHarness<Identifier> createUtilCompiler(ShaderSources sources) {
        return UTIL.program().link(UTIL.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.COMPUTE)
                .nameMapper(resourceLocation -> "utilities/" + ResourceUtil.toDebugFileNameNoExtension(resourceLocation))
                .requireExtensions(COMPUTE_EXTENSIONS).define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE).withResource(s -> s))
            .harness("utilities", sources);
    }

    static void setInstance(@Nullable IndirectPrograms newInstance) {
        if (instance != null) {
            instance.release();
        }
        if (newInstance != null) {
            newInstance.acquire();
        }
        instance = newInstance;
    }

    @Nullable
    public static IndirectPrograms get() {
        return instance;
    }

    public static boolean allLoaded() {
        return instance != null;
    }

    public static void kill() {
        setInstance(null);
    }

    public GlProgram getIndirectProgram(InstanceType<?> instanceType, ContextShader contextShader, Material material, PipelineCompiler.OitMode oit) {
        return pipeline.get(instanceType, contextShader, material, oit);
    }

    public GlProgram getCullingProgram(InstanceType<?> instanceType) {
        return culling.get(instanceType);
    }

    public GlProgram getApplyProgram() {
        return utils.get(APPLY_SHADER_MAIN);
    }

    public GlProgram getScatterProgram() {
        return utils.get(SCATTER_SHADER_MAIN);
    }

    public GlProgram getDownsampleFirstProgram() {
        return utils.get(DOWNSAMPLE_FIRST);
    }

    public GlProgram getDownsampleSecondProgram() {
        return utils.get(DOWNSAMPLE_SECOND);
    }

    public OitPrograms oitPrograms() {
        return oitPrograms;
    }

    @Override
    protected void _delete() {
        pipeline.delete();
        culling.delete();
        utils.delete();
        oitPrograms.delete();
    }
}
