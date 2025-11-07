package com.zurrtum.create.client.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.backend.gl.GlCompat;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.glsl.GlslVersion;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.util.AtomicReferenceCounted;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstancingPrograms extends AtomicReferenceCounted {
    private static final List<String> EXTENSIONS = getExtensions(GlCompat.MAX_GLSL_VERSION);

    @Nullable
    private static InstancingPrograms instance;

    private final PipelineCompiler pipeline;

    private final OitPrograms oitPrograms;

    private InstancingPrograms(PipelineCompiler pipeline, OitPrograms oitPrograms) {
        this.pipeline = pipeline;
        this.oitPrograms = oitPrograms;
    }

    private static List<String> getExtensions(GlslVersion glslVersion) {
        var extensions = ImmutableList.<String>builder();
        if (glslVersion.compareTo(GlslVersion.V330) < 0) {
            extensions.add("GL_ARB_shader_bit_encoding");
        }
        return extensions.build();
    }

    static void reload(ShaderSources sources, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
        if (!GlCompat.SUPPORTS_INSTANCING) {
            return;
        }

        var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INSTANCING, vertexComponents, fragmentComponents, EXTENSIONS);
        var fullscreen = OitPrograms.createFullscreenCompiler(sources);
        InstancingPrograms newInstance = new InstancingPrograms(pipelineCompiler, fullscreen);

        setInstance(newInstance);
    }

    static void setInstance(@Nullable InstancingPrograms newInstance) {
        if (instance != null) {
            instance.release();
        }
        if (newInstance != null) {
            newInstance.acquire();
        }
        instance = newInstance;
    }

    @Nullable
    public static InstancingPrograms get() {
        return instance;
    }

    public static boolean allLoaded() {
        return instance != null;
    }

    public static void kill() {
        setInstance(null);
    }

    public GlProgram get(InstanceType<?> instanceType, ContextShader contextShader, Material material, PipelineCompiler.OitMode mode) {
        return pipeline.get(instanceType, contextShader, material, mode);
    }

    public OitPrograms oitPrograms() {
        return oitPrograms;
    }

    @Override
    protected void _delete() {
        pipeline.delete();
        oitPrograms.delete();
    }
}
