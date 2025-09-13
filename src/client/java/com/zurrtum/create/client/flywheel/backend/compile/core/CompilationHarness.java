package com.zurrtum.create.client.flywheel.backend.compile.core;

import com.zurrtum.create.client.flywheel.backend.gl.GlObject;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;

import java.util.HashMap;
import java.util.Map;

public class CompilationHarness<K> {
    private final ShaderSources sources;
    private final KeyCompiler<K> compiler;
    private final ShaderCache shaderCache;
    private final ProgramLinker programLinker;

    private final Map<K, GlProgram> programs = new HashMap<>();

    public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
        this.sources = sources;
        this.compiler = compiler;
        shaderCache = new ShaderCache();
        programLinker = new ProgramLinker();
    }

    public GlProgram get(K key) {
        return programs.computeIfAbsent(key, this::compile);
    }

    private GlProgram compile(K key) {
        return compiler.compile(key, sources, shaderCache, programLinker);
    }

    public void delete() {
        shaderCache.delete();

        programs.values().forEach(GlObject::delete);

        programs.clear();
    }

    public interface KeyCompiler<K> {
        GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker);
    }
}
