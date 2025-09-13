package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Vector4f;

public final class FogUniforms extends UniformWriter {
    private static final int SIZE = 4 * 8;
    static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);
    static final float[] CACHE = new float[8];

    public static void update(
        Vector4f fogColor,
        float environmentalStart,
        float environmentalEnd,
        float renderDistanceStart,
        float renderDistanceEnd
    ) {
        CACHE[0] = fogColor.x;
        CACHE[1] = fogColor.y;
        CACHE[2] = fogColor.z;
        CACHE[3] = fogColor.w;
        CACHE[4] = environmentalStart;
        CACHE[5] = environmentalEnd;
        CACHE[6] = renderDistanceStart;
        CACHE[7] = renderDistanceEnd;
    }

    public static void update(GpuBufferSlice shaderFog) {
        long ptr = BUFFER.ptr();

        if (shaderFog.buffer().usage() == 128) {
            ptr = writeFloat(ptr, 0);
            ptr = writeFloat(ptr, 0);
            ptr = writeFloat(ptr, 0);
            ptr = writeFloat(ptr, 0);
            ptr = writeFloat(ptr, Float.MAX_VALUE);
            ptr = writeFloat(ptr, Float.MAX_VALUE);
            ptr = writeFloat(ptr, Float.MAX_VALUE);
            ptr = writeFloat(ptr, Float.MAX_VALUE);
        } else {
            ptr = writeFloat(ptr, CACHE[0]);
            ptr = writeFloat(ptr, CACHE[1]);
            ptr = writeFloat(ptr, CACHE[2]);
            ptr = writeFloat(ptr, CACHE[3]);
            ptr = writeFloat(ptr, CACHE[4]);
            ptr = writeFloat(ptr, CACHE[5]);
            ptr = writeFloat(ptr, CACHE[6]);
            ptr = writeFloat(ptr, CACHE[7]);
        }

        BUFFER.markDirty();
    }
}
