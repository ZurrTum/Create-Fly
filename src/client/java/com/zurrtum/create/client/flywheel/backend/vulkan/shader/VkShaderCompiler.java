package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.glsl.VkAssembly;
import com.zurrtum.create.client.flywheel.backend.glsl.VkGLSLAssembler;

import net.minecraft.client.Minecraft;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import net.vulkanmod.vulkan.shader.SPIRVUtils.SPIRV;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Turns assembled GL-flavoured GLSL into SPIR-V for the Vulkan backend.
 *
 * <p>Three steps, and only the middle one is interesting:
 * <ol>
 *   <li>The text arrives already flattened and versioned by {@link VkShaderSource}.</li>
 *   <li>{@link VkGLSLAssembler} (Agent A) rewrites it into valid Vulkan GLSL.</li>
 *   <li>{@link SPIRVUtils#compileShader} runs shaderc.</li>
 * </ol>
 *
 * <p>Step 3 is worth understanding rather than treating as a black box, because
 * shaderc's configuration is fixed by VulkanMod and we cannot change it:
 * <ul>
 *   <li>Target is {@code vulkan_1_2} — so {@code #version 450} is correct and no
 *       SPIR-V 1.5+ feature may be used.</li>
 *   <li>{@code generate_debug_info} is on, so the {@code #line} directives survive
 *       into the SPIR-V and shaderc error messages still point at the right source
 *       line. Keep them.</li>
 *   <li>Optimisation is off. Fine for v1 — correctness first.</li>
 *   <li>Failure throws {@link RuntimeException} carrying shaderc's full message.
 *       Nothing catches it: a shader that does not compile should fail loudly at
 *       startup rather than render garbage. Callers that want it to stick without
 *       re-running shaderc should use {@link VkProgramCache}, which reproduces
 *       the GL backend's cache-the-failure behaviour.</li>
 * </ul>
 */
public final class VkShaderCompiler {

    /**
     * Mirrors {@code Compilation#DUMP_SHADER_SOURCE}. Without a dump, debugging a
     * Vulkan-side shader problem is guesswork, since the text is assembled from
     * resources, generated Java and inline includes.
     *
     * <p>Enable with {@code -Dflw.dumpShaderSource}. Output goes to
     * {@code <gameDir>/flywheel_sources/} and uses the same file names as the GL
     * backend, so the two are directly diffable.
     */
    public static final boolean DUMP_SHADER_SOURCE = System.getProperty("flw.dumpShaderSource") != null;

    private VkShaderCompiler() {
    }

    /**
     * Assembles, rewrites and compiles one shader stage.
     *
     * @param name       debug name for the stage, used in error messages and the
     *                   dumped file name; not a filesystem path
     * @param stage      which stage is being compiled
     * @param glslSource the flat GL-flavoured GLSL from {@link VkShaderSource#build()}
     * @return the compiled SPIR-V module, owned by the caller
     * @throws RuntimeException wrapping shaderc's diagnostic if compilation fails
     */
    public static SPIRV compile(String name, VkShaderStage stage, String glslSource) {
        String source = prepare(name, stage, glslSource);

        return SPIRVUtils.compileShader(name + stage.extension(), source, stage.kind());
    }

    /**
     * Runs the OpenGL-to-Vulkan rewrite and returns text ready for shaderc.
     *
     * <p>Separated from {@link #compile} because VulkanMod compiles both stages
     * itself, inside {@code GraphicsPipeline}'s constructor — there is no way to
     * hand it pre-compiled SPIR-V. Caching the rewritten <em>source</em> is
     * therefore the useful thing to cache: it skips the Java-side assembly and the
     * binding-injection passes on a cache hit, while leaving shaderc to run when
     * the pipeline is actually built. See {@link VkProgramCache}.
     *
     * <p>Side effects, all intentional and all idempotent: logs anything the
     * assembler dropped, and writes a dump when {@link #DUMP_SHADER_SOURCE} is on.
     */
    public static String prepare(String name, VkShaderStage stage, String glslSource) {
        VkAssembly assembly = VkGLSLAssembler.assemble(glslSource, stage.glslName());

        reportDropped(name, stage, assembly);

        String fileName = name + stage.extension();
        dumpSource(assembly.source(), fileName);

        return assembly.source();
    }

    /**
     * Surfaces everything the assembler had to remove.
     *
     * <p>This package deliberately never emits the OpenGL-only extensions or the
     * {@code fma} macro (see {@link VkShaderSource#enableExtension}), so in normal
     * operation this lists nothing. It is kept because the assembler also inspects
     * text coming from resource files, which nothing here controls: if a future
     * material shader gains an OpenGL-ism, this line is how it surfaces instead of
     * as an unexplained compile failure.
     */
    private static void reportDropped(String name, VkShaderStage stage, VkAssembly assembly) {
        List<String> extensions = assembly.droppedExtensions();
        List<String> defines = assembly.droppedDefines();

        if (extensions.isEmpty() && defines.isEmpty()) {
            return;
        }

        FlwBackend.LOGGER.warn(
            "[FW/VK] {} ({}) lost OpenGL-only constructs in the Vulkan rewrite. "
                + "Each one is a silent behaviour change, not just dead text.",
            name, stage.glslName()
        );

        if (!extensions.isEmpty()) {
            FlwBackend.LOGGER.warn("[FW/VK]   dropped #extension: {}", extensions);
        }
        if (!defines.isEmpty()) {
            FlwBackend.LOGGER.warn("[FW/VK]   dropped #define: {}", defines);
        }
    }

    private static void dumpSource(String source, String fileName) {
        if (!DUMP_SHADER_SOURCE) {
            return;
        }

        File file = new File(new File(Minecraft.getInstance().gameDirectory, "flywheel_sources"), fileName);

        // mkdirs the parent so we don't create a directory named by the leaf file.
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(source);
        } catch (Exception e) {
            FlwBackend.LOGGER.error("Could not dump shader source for " + fileName, e);
        }
    }
}