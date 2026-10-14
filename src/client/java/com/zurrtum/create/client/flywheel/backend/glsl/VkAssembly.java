package com.zurrtum.create.client.flywheel.backend.glsl;

import java.util.List;

/**
 * The result of rewriting OpenGL-flavoured GLSL for Vulkan.
 *
 * <p>{@link #source()} is the only field callers normally need. The two
 * {@code dropped*} lists exist so that <em>every</em> removal the rewrite performed
 * is observable, whether or not it mattered. A dropped {@code #extension} or
 * {@code #define} is not just dead text: it is a promise that some behaviour will
 * silently not happen, so whoever consumes this is expected to log them.
 *
 * @param source            rewritten GLSL, ready for shaderc
 * @param droppedExtensions names of the OpenGL-only constructs that were removed:
 *                          {@code GL_*} {@code #extension} directives, plus the
 *                          {@code layout(depth_*) out float gl_FragDepth;}
 *                          redeclaration belonging to
 *                          {@code GL_ARB_conservative_depth}. Never {@code null}
 * @param droppedDefines    names of {@code #define} macros that were removed,
 *                          never {@code null}
 */
public record VkAssembly(
    String source,
    List<String> droppedExtensions,
    List<String> droppedDefines
) {

    /**
     * The drop lists are always empty here, so the javadoc's "never null" promise
     * holds for a hand-built instance too.
     */
    public VkAssembly {
        droppedExtensions = List.copyOf(droppedExtensions);
        droppedDefines = List.copyOf(droppedDefines);
    }

    /**
     * Whether the rewrite removed anything at all.
     */
    public boolean droppedAnything() {
        return !droppedExtensions.isEmpty() || !droppedDefines.isEmpty();
    }
}