package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import net.vulkanmod.vulkan.shader.SPIRVUtils.ShaderKind;

/**
 * The shader stages Flywheel needs, paired with the VulkanMod
 * {@link ShaderKind} they compile to.
 *
 * <p>The {@link #glslName()} values are the canonical strings handed to
 * {@code VkGLSLAssembler#assemble(String, String)}. They are deliberately plain
 * strings rather than {@code ShaderType}, because {@code ShaderType} carries LWJGL
 * OpenGL enum constants and this package must not depend on GL at all.
 */
public enum VkShaderStage {
    VERTEX("vertex", ".vert", ShaderKind.VERTEX_SHADER),
    FRAGMENT("fragment", ".frag", ShaderKind.FRAGMENT_SHADER),
    COMPUTE("compute", ".comp", ShaderKind.COMPUTE_SHADER);

    private final String glslName;
    private final String extension;
    private final ShaderKind kind;

    VkShaderStage(String glslName, String extension, ShaderKind kind) {
        this.glslName = glslName;
        this.extension = extension;
        this.kind = kind;
    }

    /**
     * {@code "vertex"} / {@code "fragment"} / {@code "compute"}.
     */
    public String glslName() {
        return glslName;
    }

    /**
     * File extension used when dumping shader source to disk, matching the GL
     * backend's naming so dumped files stay comparable.
     */
    public String extension() {
        return extension;
    }

    public ShaderKind kind() {
        return kind;
    }

    /**
     * Resolves the {@code stage} string produced by {@link #glslName()} back to
     * the enum.
     *
     * @throws IllegalArgumentException if the string names no known stage
     */
    public static VkShaderStage byGlslName(String stage) {
        for (VkShaderStage value : values()) {
            if (value.glslName.equals(stage)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown shader stage: " + stage);
    }
}