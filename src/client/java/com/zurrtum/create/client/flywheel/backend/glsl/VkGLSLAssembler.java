package com.zurrtum.create.client.flywheel.backend.glsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Rewrites OpenGL-flavoured GLSL into the subset that Vulkan GLSL accepts.
 *
 * <p>This is the whole of the OpenGL-to-Vulkan translation Flywheel needs at the
 * <em>preprocessor</em> level. Flywheel's shader set is already modern core GLSL —
 * {@code in}/{@code out} rather than {@code attribute}/{@code varying},
 * {@code texture()} rather than {@code texture2D()}, explicit {@code out vec4}
 * fragment outputs, and {@code layout(std140)} uniform blocks — so the only
 * OpenGL-specific things left in the text are a handful of directives. That was
 * verified across every shader resource, not assumed: {@code texture2D},
 * {@code texture3D}, {@code gl_FragColor}, {@code gl_FragData}, {@code varying}
 * and {@code attribute} have zero hits.
 *
 * <p><b>What it does, and why each item is there:</b>
 * <ul>
 *   <li><b>{@code #version} &rarr; 450.</b> The OpenGL path injects
 *       {@code GlCompat.MAX_GLSL_VERSION}, which resolves to {@code 150} whenever
 *       capability detection fails — i.e. always under VulkanMod. GLSL 150 cannot
 *       compile this shader set at all.</li>
 *   <li><b>Drops {@code #extension GL_*}.</b> Vulkan GLSL has no OpenGL extension
 *       mechanism; everything in the Vulkan GLSL 4.50 feature set is core. The
 *       concrete casualty is {@code GL_ARB_conservative_depth}, which the OpenGL
 *       pipeline enables unconditionally and which switches on a redeclaration of
 *       {@code gl_FragDepth} — a reserved name that may not be redeclared in
 *       Vulkan GLSL, so its presence is a hard compile error rather than a silent
 *       difference. Dropping the directive makes the
 *       {@code #if defined(GL_ARB_conservative_depth)} guard around it evaluate
 *       false, which retires the whole branch. The only cost is the early-Z
 *       hint it was providing.</li>
 *   <li><b>Drops {@code #define fma(...)}.</b> The OpenGL path defines this as a
 *       fallback because GLSL 1.50 has no {@code fma}. GLSL 4.50 does, and a
 *       function-like macro of the same name shadows the built-in, so leaving it
 *       in place silently downgrades every fused multiply-add to a separate
 *       multiply and add. {@code wavelet.glsl} genuinely calls {@code fma()} and
 *       is on the instancing path.</li>
 *   <li><b>Drops the {@code gl_FragDepth} redeclaration.</b> The OpenGL path writes
 *       {@code layout (depth_greater) out float gl_FragDepth;} to get an early-Z
 *       depth hint, guarded by {@code #if defined(GL_ARB_conservative_depth)}.
 *       Retiring the extension already makes that guard false, so the line is inert
 *       — but leaning on a guard that nothing enforces is fragile, because any
 *       future source that enables the extension would reactivate a construct that
 *       is a hard compile error in Vulkan GLSL. The declaration itself is therefore
 *       removed. Writes to {@code gl_FragDepth} are legal in Vulkan and are left
 *       untouched.</li>
 * </ul>
 *
 * <p><b>Scope boundary.</b> This class is purely lexical: it deletes directives and
 * rewrites one line. It deliberately does <em>not</em> assign
 * {@code layout(binding = N)} or {@code layout(location = N)}, and does not
 * rewrite {@code usamplerBuffer}. Those are structural changes that depend on the
 * descriptor layout of the pipeline being built, and they are done by the caller
 * against tables derived from VulkanMod. Keeping the split here means this class
 * needs no tables and no configuration, and two callers with different descriptor
 * layouts can both use it.
 *
 * <p><b>Line numbers are preserved.</b> Removed lines are blanked rather than
 * deleted. Everything this class removes lives in the preamble, before the first
 * {@code #line} directive, so Flywheel's {@code #line}-based error mapping is
 * unaffected either way — but blanking also keeps the rewritten text line-for-line
 * comparable with the OpenGL backend's dump, which is what makes
 * {@code -Dflw.dumpShaderSource} useful for diffing the two.
 *
 * <p><b>Idempotent.</b> Running it on already-rewritten text changes nothing, so
 * it is safe in a pipeline that may apply more than one rewrite pass.
 */
public final class VkGLSLAssembler {

    /**
     * The GLSL version every Vulkan shader is emitted at. VulkanMod compiles with
     * {@code shaderc_env_version_vulkan_1_2}, for which 450 is the correct pairing.
     */
    public static final int VULKAN_GLSL_VERSION = 450;

    private static final String VULKAN_GLSL_VERSION_LINE = "#version " + VULKAN_GLSL_VERSION;

    /**
     * Matches the OpenGL form of declaring a fragment depth output, e.g.
     * {@code layout (depth_greater) out float gl_FragDepth;}.
     *
     * <p>{@code gl_FragDepth} is a reserved built-in name and may not be redeclared
     * in Vulkan GLSL, so this is a hard error rather than a behavioural
     * difference. <b>Only the declaration form is matched</b>: a plain assignment
     * such as {@code gl_FragDepth = delinearize_depth(...)} is legal in Vulkan and is
     * left alone, which matters because {@code oit_depth.frag} and
     * {@code oit_composite.frag} both write it.
     */
    private static final Pattern FRAG_DEPTH_REDECLARATION = Pattern.compile(
        "^[ \\t]*layout[ \\t]*\\([^)]*\\)[ \\t]*out[ \\t]+float[ \\t]+gl_FragDepth[ \\t]*;[ \\t]*\\r?$",
        Pattern.MULTILINE);

    /**
     * The stage names this assembler accepts. Deliberately plain strings, matching
     * the {@code stage} argument rather than any backend enum, so that this class
     * stays independent of both the OpenGL and Vulkan shader packages.
     */
    private static final Set<String> STAGES = Set.of("vertex", "fragment", "compute");

    private VkGLSLAssembler() {
    }

    /**
     * Rewrites {@code glslSource} for Vulkan.
     *
     * @param glslSource the assembled, include-flattened OpenGL-flavoured GLSL
     * @param stage      one of {@code "vertex"}, {@code "fragment"},
     *                   {@code "compute"}; validated so that a typo fails here
     *                   rather than silently producing a shader for the wrong stage
     * @return the rewritten source plus everything that was removed
     * @throws IllegalArgumentException if {@code stage} is not recognised, or
     *                                  {@code glslSource} is {@code null}
     */
    public static VkAssembly assemble(String glslSource, String stage) {
        if (glslSource == null) {
            throw new IllegalArgumentException("glslSource is null");
        }
        if (!STAGES.contains(stage)) {
            throw new IllegalArgumentException(
                "Unknown shader stage '" + stage + "'; expected one of " + STAGES
            );
        }

        var droppedExtensions = new ArrayList<String>();
        var droppedDefines = new ArrayList<String>();

        // split with a negative limit so trailing empty lines survive: the rewrite
        // must not change the line count, since callers compare against dumps and
        // rely on #line directives for error attribution.
        String[] lines = glslSource.split("\n", -1);

        boolean sawVersion = false;
        int versionLineIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (isVersionDirective(line)) {
                sawVersion = true;
                versionLineIndex = i;
                lines[i] = VULKAN_GLSL_VERSION_LINE;
                continue;
            }

            String extension = glExtensionName(line);
            if (extension != null) {
                lines[i] = "";
                if (!droppedExtensions.contains(extension)) {
                    droppedExtensions.add(extension);
                }
                continue;
            }

            if (isFmaDefine(line)) {
                lines[i] = "";
                if (!droppedDefines.contains("fma(a, b, c)")) {
                    droppedDefines.add("fma(a, b, c)");
                }
                continue;
            }

            if (FRAG_DEPTH_REDECLARATION.matcher(line).matches()) {
                lines[i] = "";
                // Reported alongside the extension, because retiring this
                // declaration is part of retiring GL_ARB_conservative_depth: the
                // early-Z depth hint is the behaviour that goes away.
                if (!droppedExtensions.contains("layout(depth_*) out gl_FragDepth")) {
                    droppedExtensions.add("layout(depth_*) out gl_FragDepth");
                }
            }
        }

        if (!sawVersion) {
            // No #version at all: prepend one. This shifts every line down by one,
            // which is unavoidable, and is why callers should emit a version line
            // themselves rather than rely on this path.
            String[] withVersion = new String[lines.length + 1];
            withVersion[0] = VULKAN_GLSL_VERSION_LINE;
            System.arraycopy(lines, 0, withVersion, 1, lines.length);
            lines = withVersion;
        } else if (versionLineIndex > 0) {
            // #version has to be the first non-comment token, so hoist it to line 1
            // and blank the slot it came from. Blank-rather-than-delete keeps the
            // line count identical, so any #line directives further down still
            // resolve to the same lines they did before.
            lines[versionLineIndex] = "";
            lines[0] = VULKAN_GLSL_VERSION_LINE;
        }

        return new VkAssembly(String.join("\n", lines), droppedExtensions, droppedDefines);
    }

    /**
     * Whether the line is a {@code #version} directive, in either the
     * {@code #version 150} or spaced {@code # version 150 core} form.
     *
     * <p>Stricter than a bare keyword match: the keyword must be followed by
     * whitespace or end of line, so that a hypothetical {@code #versionFoo} is not
     * mistaken for the directive and rewritten.
     */
    private static boolean isVersionDirective(String line) {
        int end = directiveKeywordEnd(line, "version");
        if (end < 0) {
            return false;
        }

        return end == line.length() || line.charAt(end) == ' ' || line.charAt(end) == '\t';
    }

    /**
     * Returns the extension name if the line enables a {@code GL_*} extension, else
     * {@code null}.
     *
     * <p>Only {@code GL_}-prefixed names are matched. Vulkan GLSL does have a
     * handful of non-GL extensions ({@code GL_EXT_...} in SPIR-V terms are still
     * OpenGL ones, but things like {@code SPV_KHR_...} exist), and blanket-dropping
     * every directive would make it impossible to use one.
     */
    private static String glExtensionName(String line) {
        int after = directiveKeywordEnd(line, "extension");
        if (after < 0) {
            return null;
        }

        String rest = line.substring(after).trim();
        if (rest.startsWith("GL_")) {
            // Trim the behaviour suffix (: enable / : require / : warn) and any
            // trailing carriage return.
            int colon = rest.indexOf(':');
            if (colon >= 0) {
                rest = rest.substring(0, colon);
            }
            return rest.trim();
        }

        return null;
    }

    /**
     * Whether the line is a function-like {@code #define} of {@code fma}.
     *
     * <p>Matches any arity. {@code fma} is a reserved built-in in GLSL 4.50, so any
     * macro of that name is shadowing it and is always a downgrade.
     */
    private static boolean isFmaDefine(String line) {
        int after = directiveKeywordEnd(line, "define");
        if (after < 0) {
            return false;
        }

        String rest = line.substring(after).trim();

        int paren = rest.indexOf('(');
        if (paren < 0) {
            // Object-like macro (not a function-like one) that happens to be named
            // fma; still shadows the built-in, so still drop it.
            return rest.equals("fma");
        }

        return rest.substring(0, paren).trim().equals("fma");
    }

    /**
     * Index just past {@code keyword} if the line is a {@code #} directive naming
     * it, else {@code -1}.
     *
     * <p>Skips leading whitespace, requires a {@code #}, skips whitespace again,
     * then matches the keyword. Callers that care about what follows the keyword
     * use the returned index to check it themselves.
     */
    private static int directiveKeywordEnd(String line, String keyword) {
        int i = 0;

        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }

        if (i >= line.length() || line.charAt(i) != '#') {
            return -1;
        }
        i++;

        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }

        if (!line.regionMatches(i, keyword, 0, keyword.length())) {
            return -1;
        }

        return i + keyword.length();
    }
}