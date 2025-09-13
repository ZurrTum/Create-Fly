package com.zurrtum.create.client.flywheel.backend.compile.core;

import com.zurrtum.create.client.flywheel.backend.gl.shader.GlShader;
import com.zurrtum.create.client.flywheel.backend.gl.shader.ShaderType;
import com.zurrtum.create.client.flywheel.backend.glsl.GlslVersion;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;

import java.util.*;
import java.util.function.Consumer;

public class ShaderCache {
    private final Map<ShaderKey, ShaderResult> inner = new HashMap<>();

    public ShaderCache() {
    }

    public GlShader compile(
        GlslVersion glslVersion,
        ShaderType shaderType,
        String name,
        Consumer<Compilation> callback,
        List<SourceComponent> sourceComponents
    ) {
        var key = new ShaderKey(glslVersion, shaderType, name);
        var cached = inner.get(key);
        if (cached != null) {
            return cached.unwrap();
        }

        Compilation ctx = new Compilation();
        ctx.version(glslVersion);
        ctx.define(shaderType.define);

        callback.accept(ctx);

        expand(sourceComponents, ctx::appendComponent);

        ShaderResult out = ctx.compile(shaderType, name);
        inner.put(key, out);
        return out.unwrap();
    }

    public void delete() {
        inner.values().stream().filter(r -> r instanceof ShaderResult.Success).map(ShaderResult::unwrap).forEach(GlShader::delete);
        inner.clear();
    }

    private static void expand(List<SourceComponent> rootSources, Consumer<SourceComponent> out) {
        var included = new LinkedHashSet<SourceComponent>(); // use hash set to deduplicate. linked to preserve order
        for (var component : rootSources) {
            recursiveDepthFirstInclude(included, component);
            included.add(component);
        }
        included.forEach(out);
    }

    private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
        for (var include : component.included()) {
            recursiveDepthFirstInclude(included, include);
        }
        included.addAll(component.included());
    }

    private record ShaderKey(GlslVersion glslVersion, ShaderType shaderType, String name) {
    }
}
