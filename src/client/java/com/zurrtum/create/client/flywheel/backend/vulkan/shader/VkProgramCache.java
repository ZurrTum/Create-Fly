package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Caches rewritten shader source and built pipelines for one set of pipeline
 * definitions.
 *
 * <p>Stands in for {@code ShaderCache} and {@code CompilationHarness}, both of
 * which live under {@code backend/compile} and are typed to GL objects
 * ({@code Map<ShaderKey, ShaderResult>} holding {@code GlShader} handles, and
 * {@code Map<K, GlProgram>}). The layering is reproduced here rather than reused,
 * but the observable semantics are kept identical so that behaviour does not
 * depend on which backend produced it.
 *
 * <p><b>Two levels, because they cache different things.</b> The source level
 * skips Java-side assembly and the binding-injection passes; shaderc still runs on
 * a hit, inside {@code GraphicsPipeline}'s constructor, because VulkanMod offers no
 * way to supply pre-compiled SPIR-V. The program level skips that too, and skips
 * {@code vkCreateGraphicsPipelines}.
 *
 * <p><b>Failure caching is inherited deliberately.</b> {@code ShaderCache#compile}
 * stores its result before unwrapping it, so a failed compile is cached and every
 * later lookup rethrows the same failure immediately instead of re-running shaderc.
 * That is the right behaviour: a shader that failed to compile once will fail
 * again, and retrying on every frame of a broken material would stall the render
 * thread. It also means a transient failure is sticky until
 * {@link #delete()}, which matches how the GL backend recovers — by reloading
 * resources, which rebuilds the whole cache.
 *
 * @param <K> the pipeline key, in practice something equivalent to
 *            {@code PipelineCompiler.PipelineProgramKey}
 */
public final class VkProgramCache<K> {

    private final Map<ShaderKey, PreparedShader> shaders = new HashMap<>();
    private final Map<K, VkProgram> programs = new HashMap<>();

    public VkProgramCache() {
    }

    /**
     * Returns the Vulkan-ready source for one stage, assembling and rewriting it on
     * a miss.
     *
     * <p>On a miss that throws, the failure is cached and rethrown. On a later
     * lookup the stored failure is rethrown without re-attempting compilation.
     *
     * @param name       debug name for the stage, unique per distinct shader
     * @param stage      which stage
     * @param glslSource freshly assembled GL-flavoured GLSL; only read on a miss
     * @return text ready for shaderc
     */
    public String prepare(String name, VkShaderStage stage, String glslSource) {
        var key = new ShaderKey(stage, name);

        PreparedShader cached = shaders.get(key);
        if (cached != null) {
            return cached.unwrap(name);
        }

        PreparedShader out;
        try {
            out = new PreparedShader(VkShaderCompiler.prepare(name, stage, glslSource), null);
        } catch (RuntimeException e) {
            out = new PreparedShader(null, e);
        }

        shaders.put(key, out);

        return out.unwrap(name);
    }

    /**
     * Returns the pipeline for {@code key}, building it on first request.
     *
     * @param factory receives the key and returns a built pipeline; called at most
     *                once per key
     */
    public VkProgram get(K key, Function<K, VkProgram> factory) {
        return programs.computeIfAbsent(key, factory);
    }

    /**
     * Number of distinct pipelines currently held. Useful in tests and when
     * reasoning about what a reload actually costs.
     */
    public int size() {
        return programs.size();
    }

    /**
     * Releases every cached pipeline and forgets every cached shader.
     *
     * <p>Mirrors {@code CompilationHarness#delete}, which clears the program map
     * and deletes each {@code GlProgram}. Programs are scheduled for destruction on
     * VulkanMod's frame boundary rather than freed immediately, since Vulkan
     * objects cannot be destroyed while in flight.
     */
    public void delete() {
        programs.values().forEach(VkProgram::delete);
        programs.clear();
        shaders.clear();

        FlwBackend.LOGGER.info("[FW/VK] program cache cleared");
    }

    /**
     * Key for the source cache.
     *
     * <p>Mirrors {@code ShaderCache.ShaderKey}, minus the {@code GlslVersion}
     * component: that field existed to record which GLSL version the GL driver
     * supported, and this path always emits 450. {@code name} still has to uniquely
     * identify the source — the GL cache relies on the same property, and relies on
     * it just as hard, since the key holds no source text.
     */
    private record ShaderKey(VkShaderStage stage, String name) {
    }

    /**
     * Either a rewritten shader or the failure that produced while rewriting it.
     *
     * <p>Exactly one of the two fields is non-null.
     */
    private record PreparedShader(String source, RuntimeException failure) {

        String unwrap(String name) {
            if (failure != null) {
                throw failure;
            }
            return source;
        }
    }
}