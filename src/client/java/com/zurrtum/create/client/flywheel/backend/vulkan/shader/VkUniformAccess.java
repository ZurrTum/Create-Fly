package com.zurrtum.create.client.flywheel.backend.vulkan.shader;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

/**
 * The contract Agent C's uniform layer implements so that {@link VkProgram} can
 * offer the same surface as {@code GlProgram}.
 *
 * <p><b>Why this exists.</b> {@code GlProgram} writes uniforms through
 * {@code glUniform*} using a location looked up by name at link time. Vulkan has
 * no such thing: uniform data lives inside a {@code uniform} block that is bound
 * as a whole descriptor, and each member is written at a byte offset that the
 * backend computes from the block's std140 layout. That layout knowledge belongs
 * to the uniform layer, not to the program handle.
 *
 * <p><b>Contract expected by implementors.</b>
 * <ul>
 *   <li>Implementations may assume every name passed here is a member of one of
 *       Flywheel's five std140 blocks ({@code _FlwFrameUniforms},
 *       {@code _FlwFogUniforms}, {@code _FlwOptionsUniforms},
 *       {@code _FlwPlayerUniforms}, {@code _FlwLevelUniforms}).</li>
 *   <li>A name that resolves to nothing should be ignored, not throw. The GL
 *       backend tolerates unused uniforms (see {@code GlProgram#getUniformLocation},
 *       which only logs at debug level), and shader permutations legitimately
 *       leave uniforms undeclared.</li>
 *   <li>Implementations need not be thread safe; they are only touched from the
 *       render thread.</li>
 * </ul>
 *
 * <p>{@link VkProgram} treats a {@code null} uniform accessor as "uniforms are
 * not wired up yet" and silently no-ops every setter, so this interface is safe
 * to leave unimplemented during bring-up.
 */
public interface VkUniformAccess {

    void setFloat(String name, float value);

    void setVec2(String name, float x, float y);

    void setVec3(String name, float x, float y, float z);

    void setVec4(String name, float x, float y, float z, float w);

    void setMat4(String name, Matrix4fc matrix);

    void setMat3(String name, Matrix3fc matrix);

    void setInt(String name, int value);

    void setUInt(String name, int value);

    /**
     * Writes a packed {@code uvec2}. Mirrors {@code GlProgram#setUVec2}, which
     * backs Flywheel's {@code _flw_packedMaterial} / {@code _flw_vertexOffset}
     * uniforms.
     */
    void setUVec2(String name, int x, int y);

    default void setBool(String name, boolean value) {
        setInt(name, value ? 1 : 0);
    }

    /**
     * Whether this accessor knows how to write {@code name}. Used by
     * {@link VkProgram} to emit a one-shot warning for uniforms that silently go
     * nowhere, instead of failing every draw.
     */
    default boolean has(String name) {
        return true;
    }
}