package com.zurrtum.create.client.flywheel.backend.gl;

import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.compile.core.Compilation;
import com.zurrtum.create.client.flywheel.backend.gl.shader.GlProgram;
import com.zurrtum.create.client.flywheel.backend.glsl.GlslVersion;
import com.zurrtum.create.client.flywheel.lib.math.MoreMath;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class GlCompat {
    public static final @UnknownNullability GLCapabilities CAPABILITIES;

    static {
        GLCapabilities caps;
        try {
            caps = GL.getCapabilities();
        } catch (IllegalStateException var2) {
            FlwBackend.LOGGER.warn("Failed to get GL capabilities; default Flywheel backends will be disabled.");
            caps = null;
        }

        CAPABILITIES = caps;
    }

    public static final String GL_VENDOR_STRING = safeGetString(GL20C.GL_VENDOR);
    public static final String GL_RENDERER_STRING = safeGetString(GL20C.GL_RENDERER);
    public static final String GL_VERSION_STRING = safeGetString(GL20C.GL_VERSION);
    public static final String GL_SHADING_LANGUAGE_VERSION_STRING = safeGetString(GL20C.GL_SHADING_LANGUAGE_VERSION);

    public static final Driver DRIVER = readVendorString();
    public static final int SUBGROUP_SIZE = subgroupSize();
    public static final boolean ALLOW_DSA = true;
    public static final GlslVersion MAX_GLSL_VERSION = maxGlslVersion();

    public static final boolean SUPPORTS_DSA = isDsaSupported();

    public static final boolean SUPPORTS_INSTANCING = isInstancingSupported();
    public static final boolean SUPPORTS_INDIRECT = isIndirectSupported();

    private GlCompat() {
    }

    public static void init() {
    }

    public static int getComputeGroupCount(int invocations) {
        return MoreMath.ceilingDiv(invocations, SUBGROUP_SIZE);
    }

    public static void safeShaderSource(int glId, CharSequence source) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
            PointerBuffer pointers = stack.mallocPointer(1);
            pointers.put(sourceBuffer);
            GL20C.nglShaderSource(glId, 1, pointers.address0(), 0L);
            MemoryUtil.memFree(sourceBuffer);
        }

    }

    public static void safeMultiDrawElementsIndirect(GlProgram drawProgram, int mode, int type, int start, int end, long stride) {
        int count = end - start;
        long indirect = (long) start * stride;
        if (DRIVER == Driver.INTEL) {
            for (int i = 0; i < count; ++i) {
                drawProgram.setUInt("_flw_baseDraw", start + i);
                GL40.glDrawElementsIndirect(mode, type, indirect);
                indirect += stride;
            }
        } else {
            drawProgram.setUInt("_flw_baseDraw", start);
            GL43.glMultiDrawElementsIndirect(mode, type, indirect, count, (int) stride);
        }

    }

    private static Driver readVendorString() {
        if (CAPABILITIES == null) {
            return Driver.UNKNOWN;
        }

        // The vendor string I got was "ATI Technologies Inc."
        if (GL_VENDOR_STRING.contains("ATI") || GL_VENDOR_STRING.contains("AMD")) {
            return Driver.AMD;
        } else if (GL_VENDOR_STRING.contains("NVIDIA")) {
            return Driver.NVIDIA;
        } else if (GL_VENDOR_STRING.contains("Intel")) {
            return Driver.INTEL;
        } else if (GL_VENDOR_STRING.contains("Mesa")) {
            return Driver.MESA;
        }

        return Driver.UNKNOWN;
    }

    private static int subgroupSize() {
        if (CAPABILITIES == null) {
            return 32;
        } else if (CAPABILITIES.GL_KHR_shader_subgroup) {
            return GL31C.glGetInteger(38194);
        } else {
            return DRIVER != Driver.AMD && DRIVER != Driver.MESA ? 32 : 64;
        }
    }

    private static boolean isInstancingSupported() {
        if (CAPABILITIES == null) {
            return false;
        } else {
            return CAPABILITIES.OpenGL33 ? true : CAPABILITIES.GL_ARB_shader_bit_encoding;
        }
    }

    private static boolean isIndirectSupported() {
        if (CAPABILITIES == null) {
            return false;
        } else if (CAPABILITIES.OpenGL46) {
            return true;
        } else {
            return CAPABILITIES.GL_ARB_compute_shader && CAPABILITIES.GL_ARB_direct_state_access && CAPABILITIES.GL_ARB_gpu_shader5 && CAPABILITIES.GL_ARB_multi_bind && CAPABILITIES.GL_ARB_multi_draw_indirect && CAPABILITIES.GL_ARB_shader_draw_parameters && CAPABILITIES.GL_ARB_shader_storage_buffer_object && CAPABILITIES.GL_ARB_shading_language_420pack && CAPABILITIES.GL_ARB_vertex_attrib_binding && CAPABILITIES.GL_ARB_shader_image_load_store && CAPABILITIES.GL_ARB_shader_image_size;
        }
    }

    private static boolean isDsaSupported() {
        return CAPABILITIES == null ? false : CAPABILITIES.GL_ARB_direct_state_access;
    }

    private static GlslVersion maxGlslVersion() {
        if (CAPABILITIES == null) {
            return GlslVersion.V150;
        } else {
            GlslVersion[] glslVersions = GlslVersion.values();

            for (int i = glslVersions.length - 1; i > 0; --i) {
                GlslVersion version = glslVersions[i];
                if (canCompileVersion(version)) {
                    return version;
                }
            }

            return GlslVersion.V150;
        }
    }

    private static boolean canCompileVersion(GlslVersion version) {
        int handle = GL20.glCreateShader(35633);
        String source = "#version %d\nvoid main() {}\n".formatted(version.version);
        safeShaderSource(handle, source);
        GL20.glCompileShader(handle);
        boolean success = Compilation.compiledSuccessfully(handle);
        GL20.glDeleteShader(handle);
        return success;
    }

    /**
     * Get a non-null string from OpenGL, or "invalid" if no capabilities are available.
     */
    private static String safeGetString(int name) {
        if (CAPABILITIES == null) {
            return "invalid";
        }
        String str = GL20C.glGetString(name);
        return str == null ? "null" : str;
    }
}
