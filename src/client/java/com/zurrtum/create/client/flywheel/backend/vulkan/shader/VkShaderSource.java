package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceFile;
import com.zurrtum.create.client.flywheel.lib.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accumulates the flat GLSL text for one shader stage and normalises its
 * resource/interface declarations for Vulkan.
 *
 * <p>This is a direct port of the assembly half of
 * {@code com.zurrtum.create.client.flywheel.backend.compile.core.Compilation}
 * (lines 28-31 and 59-104) plus the include-flattening half of
 * {@code ShaderCache} (lines 49-63).
 *
 * <p>In addition to assembling components, {@link #prepareForVulkan(VkShaderStage)}
 * performs the structural declaration transforms required before {@code shaderc}
 * can compile Flywheel's GLSL under {@code shaderc_env_version_vulkan_1_2}:
 * <ol>
 *   <li><b>Option C ({@code usamplerBuffer} &rarr; {@code sampler2D}):</b>
 *       VulkanMod supports neither {@code VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER}
 *       nor {@code VK_DESCRIPTOR_TYPE_STORAGE_BUFFER}. The three buffer textures
 *       ({@code _flw_instances}, {@code _flw_lightLut}, {@code _flw_lightSections})
 *       are rewritten to 2D {@code sampler2D} textures of width
 *       {@link VkBindings#BUFFER_TEXTURE_WIDTH} backed by
 *       {@code VK_FORMAT_R8G8B8A8_UNORM} (4 bytes/texel), reconstructing exact
 *       32-bit unsigned words via GLSL 4.50's {@code packUnorm4x8}.</li>
 *   <li><b>Loose non-opaque uniforms &rarr; {@code _FlwDrawUniforms} UBO:</b>
 *       Vulkan GLSL forbids loose non-opaque {@code uniform} declarations outside a
 *       block. Per-draw uniforms in {@code instancing/main.vert} and
 *       {@code instancing/main.frag} are wrapped into
 *       {@link VkBindings#DRAW_BLOCK}.</li>
 *   <li><b>Stage {@code in}/{@code out} location qualifiers:</b>
 *       Vulkan GLSL requires explicit {@code layout(location = N)} on all vertex
 *       inputs, inter-stage varyings, and fragment outputs.</li>
 *   <li><b>Explicit descriptor bindings:</b>
 *       Injects {@code layout(std140, binding = N)} on uniform blocks and
 *       {@code layout(binding = N)} on samplers according to {@link VkBindings}.</li>
 * </ol>
 */
public final class VkShaderSource {

    /**
     * Line number {@code #line} directives use for Java-generated code. The GL
     * backend uses file 0 for "generated" and 1..N for real resource files.
     */
    private static final int GENERATED_FILE_ID = 0;

    /**
     * Matches a std140 uniform block declaration with no binding qualifier, e.g.
     * {@code layout(std140) uniform _FlwFrameUniforms {}. Tolerates the stray space
     * in {@code layout (std140)}, which {@code _FlwPlayerUniforms} uses.
     */
    private static final Pattern STD140_BLOCK = Pattern.compile(
        "layout[ \\t]*\\([ \\t]*std140[ \\t]*\\)[ \\t]+uniform[ \\t]+(\\w+)[ \\t]*\\{");

    /**
     * Matches a bare sampler declaration, e.g. {@code uniform sampler2D
     * flw_diffuseTex;}. Anchored at the start of a line so that declarations which
     * already carry a layout qualifier are left alone.
     */
    private static final Pattern LOOSE_SAMPLER = Pattern.compile(
        "^([ \\t]*)uniform[ \\t]+((?:[iu])?sampler\\w*)[ \\t]+(\\w+)[ \\t]*;",
        Pattern.MULTILINE);

    /**
     * Matches a loose non-opaque uniform declaration, e.g.
     * {@code uniform uvec2 _flw_packedMaterial;} or
     * {@code uniform int _flw_baseInstance = 0;}.
     */
    private static final Pattern LOOSE_NON_OPAQUE_UNIFORM = Pattern.compile(
        "^[ \\t]*uniform[ \\t]+(?!(?:[iu])?sampler\\w*|(?:[iu])?image\\w*)(\\w+)[ \\t]+(\\w+)(?:[ \\t]*=[ \\t]*[^;]+)?[ \\t]*;[ \\t]*\\r?\\n?",
        Pattern.MULTILINE);

    /**
     * Matches a bare top-level {@code in} or {@code out} declaration without an
     * existing {@code layout(...)} qualifier.
     */
    private static final Pattern BARE_IO_DECL = Pattern.compile(
        "^([ \\t]*)(flat[ \\t]+|noperspective[ \\t]+|smooth[ \\t]+)?(in|out)[ \\t]+(\\w+)[ \\t]+(\\w+)[ \\t]*;",
        Pattern.MULTILINE);

    /**
     * Matches {@code uniform usamplerBuffer <name>;}.
     */
    private static final Pattern USAMPLER_BUFFER_DECL = Pattern.compile(
        "^([ \\t]*)uniform[ \\t]+usamplerBuffer[ \\t]+(_flw_instances|_flw_lightLut|_flw_lightSections)[ \\t]*;",
        Pattern.MULTILINE);

    /**
     * Matches {@code texelFetch(_flw_instances, <expr>)} (1D buffer texelFetch, 2 args).
     */
    private static final Pattern INSTANCE_TEXEL_FETCH = Pattern.compile(
        "texelFetch[ \\t]*\\([ \\t]*_flw_instances[ \\t]*,[ \\t]*([^,\\r\\n]+?)\\)");

    /**
     * Matches {@code texelFetch(_flw_lightLut, <expr>)} or
     * {@code texelFetch(_flw_lightSections, <expr>)} (1D buffer texelFetch, 2 args).
     */
    private static final Pattern LIGHT_TEXEL_FETCH = Pattern.compile(
        "texelFetch[ \\t]*\\([ \\t]*(_flw_lightLut|_flw_lightSections)[ \\t]*,[ \\t]*([^,\\r\\n]+?)\\)");

    private static final String SAMPLER2D_BUFFER_HELPERS = """
        uvec4 _flw_texelFetchU32(sampler2D _flw_tex, int _flw_idx) {
            return uvec4(packUnorm4x8(texelFetch(_flw_tex, ivec2(_flw_idx & %d, _flw_idx >> %d), 0)), 0u, 0u, 0u);
        }
        uvec4 _flw_texelFetchInstance(sampler2D _flw_tex, int _flw_idx) {
            int _flw_b = _flw_idx << 2;
            return uvec4(
                packUnorm4x8(texelFetch(_flw_tex, ivec2(_flw_b & %d, _flw_b >> %d), 0)),
                packUnorm4x8(texelFetch(_flw_tex, ivec2((_flw_b + 1) & %d, (_flw_b + 1) >> %d), 0)),
                packUnorm4x8(texelFetch(_flw_tex, ivec2((_flw_b + 2) & %d, (_flw_b + 2) >> %d), 0)),
                packUnorm4x8(texelFetch(_flw_tex, ivec2((_flw_b + 3) & %d, (_flw_b + 3) >> %d), 0))
            );
        }
        """.formatted(
        VkBindings.BUFFER_TEXTURE_MASK, VkBindings.BUFFER_TEXTURE_SHIFT,
        VkBindings.BUFFER_TEXTURE_MASK, VkBindings.BUFFER_TEXTURE_SHIFT,
        VkBindings.BUFFER_TEXTURE_MASK, VkBindings.BUFFER_TEXTURE_SHIFT,
        VkBindings.BUFFER_TEXTURE_MASK, VkBindings.BUFFER_TEXTURE_SHIFT,
        VkBindings.BUFFER_TEXTURE_MASK, VkBindings.BUFFER_TEXTURE_SHIFT
    );

    private static final String DRAW_UNIFORMS_BLOCK = """
        layout(std140) uniform _FlwDrawUniforms {
            uvec2 _flw_packedMaterial;
            int _flw_baseInstance;
            uint _flw_baseVertex;
            mat4 _flw_modelMatrixUniform;
            mat3 _flw_normalMatrixUniform;
        };
        """;

    private final StringBuilder fullSource = new StringBuilder();
    private final StringBuilder generatedSource = new StringBuilder();
    private final List<SourceFile> files = new ArrayList<>();
    private int generatedLines = 0;

    /**
     * Emits {@code #version 450}.
     */
    public void version() {
        fullSource.append("#version 450\n");
    }

    /**
     * Emits the stage define ({@code VERTEX_SHADER} / {@code FRAGMENT_SHADER} /
     * {@code COMPUTE_SHADER}) and, for the vertex stage, aliases OpenGL's
     * {@code gl_VertexID} / {@code gl_InstanceID} to Vulkan GLSL's
     * {@code gl_VertexIndex} / {@code gl_InstanceIndex}.
     */
    public void stagePreamble(VkShaderStage stage) {
        switch (stage) {
            case VERTEX -> {
                define("VERTEX_SHADER");
                fullSource.append("#ifndef gl_VertexID\n#define gl_VertexID gl_VertexIndex\n#endif\n");
                fullSource.append("#ifndef gl_InstanceID\n#define gl_InstanceID gl_InstanceIndex\n#endif\n");
            }
            case FRAGMENT -> define("FRAGMENT_SHADER");
            case COMPUTE -> define("COMPUTE_SHADER");
        }
    }

    public void define(String key) {
        fullSource.append("#define ").append(key).append('\n');
    }

    public void define(String key, String value) {
        fullSource.append("#define ").append(key).append(' ').append(value).append('\n');
    }

    public void define(String key, int value) {
        define(key, Integer.toString(value));
    }

    public void enableExtension(String extension) {
        fullSource.append("#extension ").append(extension).append(" : enable\n");
    }

    public void requireExtension(String extension) {
        fullSource.append("#extension ").append(extension).append(" : require\n");
    }

    /**
     * Appends a source component, preceded by the {@code #line} directive that
     * points compiler errors back at the file or generated block it came from.
     */
    public void appendComponent(SourceComponent component) {
        var source = component.source();

        appendHeader(component, source);

        fullSource.append(source);
    }

    private void appendHeader(SourceComponent component, String source) {
        if (component instanceof SourceFile file) {
            int fileId = files.size() + 1;

            files.add(file);

            fullSource.append("\n#line 0 ").append(fileId).append(" // ").append(file.name()).append('\n');
        } else {
            generatedSource.append(source).append('\n');

            fullSource.append("\n#line ").append(generatedLines).append(' ').append(GENERATED_FILE_ID)
                .append(" // (generated) ").append(component.name()).append('\n');

            generatedLines += StringUtil.countLines(source);
        }
    }

    public List<SourceFile> files() {
        return files;
    }

    public String generatedSource() {
        return generatedSource.toString();
    }

    public String build() {
        return fullSource.toString();
    }

    /**
     * Runs the complete Vulkan normalisation pipeline on the accumulated GLSL:
     * <ol>
     *   <li>Rewrites {@code usamplerBuffer} declarations and {@code texelFetch}
     *       calls to 2D {@code sampler2D} lookups (Option C).</li>
     *   <li>Wraps loose non-opaque {@code uniform}s into
     *       {@code _FlwDrawUniforms}.</li>
     *   <li>Injects {@code layout(location = N)} on bare {@code in}/{@code out}
     *       declarations.</li>
     *   <li>Injects {@code layout(std140, binding = N)} and
     *       {@code layout(binding = N)} on uniform blocks and samplers.</li>
     * </ol>
     */
    public void prepareForVulkan(VkShaderStage stage) {
        rewriteSamplerBuffers();
        wrapLooseUniforms();
        applyLocations(stage);
        applyBindings(VkBindings.allBindings());
    }

    /**
     * Rewrites {@code _flw_instances}, {@code _flw_lightLut}, and
     * {@code _flw_lightSections} from {@code usamplerBuffer} to 2D
     * {@code sampler2D} textures (Option C).
     *
     * <p>Idempotent: if no {@code usamplerBuffer} is present in the source, nothing
     * is modified.
     */
    public void rewriteSamplerBuffers() {
        String text = fullSource.toString();
        Matcher declMatcher = USAMPLER_BUFFER_DECL.matcher(text);
        if (!declMatcher.find()) {
            return;
        }

        StringBuilder out = new StringBuilder(text.length() + 512);
        int last = 0;
        boolean helpersInserted = false;

        declMatcher.reset();
        while (declMatcher.find()) {
            out.append(text, last, declMatcher.start());
            if (!helpersInserted) {
                out.append(SAMPLER2D_BUFFER_HELPERS);
                helpersInserted = true;
            }
            out.append(declMatcher.group(1))
                .append("uniform sampler2D ")
                .append(declMatcher.group(2))
                .append(';');
            last = declMatcher.end();
        }
        out.append(text, last, text.length());

        String rewritten = INSTANCE_TEXEL_FETCH.matcher(out.toString())
            .replaceAll("_flw_texelFetchInstance(_flw_instances, $1)");
        rewritten = LIGHT_TEXEL_FETCH.matcher(rewritten)
            .replaceAll("_flw_texelFetchU32($1, $2)");

        fullSource.setLength(0);
        fullSource.append(rewritten);
    }

    /**
     * Replaces loose non-opaque {@code uniform} declarations with the shared
     * {@code _FlwDrawUniforms} std140 uniform block.
     */
    public void wrapLooseUniforms() {
        String text = fullSource.toString();
        Matcher matcher = LOOSE_NON_OPAQUE_UNIFORM.matcher(text);
        if (!matcher.find()) {
            return;
        }

        StringBuilder out = new StringBuilder(text.length() + 256);
        int last = 0;
        boolean blockInserted = false;

        matcher.reset();
        while (matcher.find()) {
            out.append(text, last, matcher.start());
            if (!blockInserted) {
                out.append(DRAW_UNIFORMS_BLOCK);
                blockInserted = true;
            }
            last = matcher.end();
        }
        out.append(text, last, text.length());

        fullSource.setLength(0);
        fullSource.append(out);
    }

    /**
     * Injects {@code layout(location = N)} into every bare {@code in} or
     * {@code out} declaration that does not already carry a {@code layout(...)}
     * qualifier.
     */
    public void applyLocations(VkShaderStage stage) {
        if (stage == VkShaderStage.COMPUTE) {
            return;
        }

        String text = fullSource.toString();
        Matcher matcher = BARE_IO_DECL.matcher(text);
        StringBuilder out = new StringBuilder(text.length() + 256);

        Map<String, Integer> inLocations = stage == VkShaderStage.VERTEX
            ? VkBindings.vertexInputLocations()
            : VkBindings.varyingLocations();
        Map<String, Integer> outLocations = stage == VkShaderStage.VERTEX
            ? VkBindings.varyingLocations()
            : VkBindings.fragmentOutputLocations();

        int last = 0;
        while (matcher.find()) {
            String indent = matcher.group(1);
            String interp = matcher.group(2) != null ? matcher.group(2) : "";
            String dir = matcher.group(3);
            String type = matcher.group(4);
            String name = matcher.group(5);

            Map<String, Integer> table = "in".equals(dir) ? inLocations : outLocations;
            Integer location = table.get(name);
            if (location == null) {
                throw new IllegalStateException(
                    "Stage " + stage.glslName() + " " + dir + " variable '" + name
                        + "' has no location in VkBindings."
                );
            }

            out.append(text, last, matcher.start());
            out.append(indent)
                .append("layout(location = ").append(location).append(") ")
                .append(interp).append(dir).append(' ').append(type).append(' ').append(name).append(';');
            last = matcher.end();
        }
        out.append(text, last, text.length());

        fullSource.setLength(0);
        fullSource.append(out);
    }

    /**
     * Injects {@code layout(binding = N)} into every uniform block and sampler
     * declaration that lacks one.
     *
     * @param bindings name to descriptor binding, covering both blocks and samplers
     */
    public void applyBindings(Map<String, Integer> bindings) {
        String text = fullSource.toString();

        String withBlocks = injectBlockBindings(text, bindings);
        String withSamplers = injectSamplerBindings(withBlocks, bindings);

        fullSource.setLength(0);
        fullSource.append(withSamplers);
    }

    private static String injectBlockBindings(String text, Map<String, Integer> bindings) {
        Matcher matcher = STD140_BLOCK.matcher(text);
        StringBuilder out = new StringBuilder(text.length() + 256);

        int last = 0;
        while (matcher.find()) {
            String name = matcher.group(1);
            Integer binding = requireBinding(name, bindings, "block");

            out.append(text, last, matcher.start());
            out.append("layout(std140, binding = ").append(binding).append(") uniform ").append(name).append(" {");

            last = matcher.end();
        }
        out.append(text, last, text.length());

        return out.toString();
    }

    private static String injectSamplerBindings(String text, Map<String, Integer> bindings) {
        Matcher matcher = LOOSE_SAMPLER.matcher(text);
        StringBuilder out = new StringBuilder(text.length() + 256);

        int last = 0;
        while (matcher.find()) {
            String indent = matcher.group(1);
            String type = matcher.group(2);
            String name = matcher.group(3);
            Integer binding = requireBinding(name, bindings, "sampler");

            out.append(text, last, matcher.start());
            out.append(indent).append("layout(binding = ").append(binding).append(") uniform ")
                .append(type).append(' ').append(name).append(';');

            last = matcher.end();
        }
        out.append(text, last, text.length());

        return out.toString();
    }

    private static Integer requireBinding(String name, Map<String, Integer> bindings, String kind) {
        Integer binding = bindings.get(name);

        if (binding == null) {
            throw new IllegalStateException(
                "Uniform " + kind + " '" + name + "' has no descriptor binding. "
                    + "Add it to VkBindings, or it will silently default to binding 0."
            );
        }

        return binding;
    }

    /**
     * Flattens a component tree into a linear, dependency-ordered list.
     *
     * <p>Port of {@code ShaderCache#expand(List, Consumer)} and
     * {@code ShaderCache#recursiveDepthFirstInclude}.
     */
    public static List<SourceComponent> expand(List<SourceComponent> roots) {
        var included = new LinkedHashSet<SourceComponent>();

        for (var root : roots) {
            recursiveDepthFirstInclude(included, root);
            included.add(root);
        }

        return new ArrayList<>(included);
    }

    private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
        for (var include : component.included()) {
            recursiveDepthFirstInclude(included, include);
        }
        included.addAll(component.included());
    }
}