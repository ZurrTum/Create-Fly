package com.zurrtum.create.client.catnip.render;

import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EntityBlockTemplateMesh {
    public final EntityBlockRenderType type;
    public final int vertexCount;
    public final Vector4fc[] positions;
    public final int[] colors;
    public final float[] uvs;
    public final int[] lights;
    public final Vector3fc @Nullable [] normals;
    private Vector4fc @Nullable [] lightPositions;
    private final ConcurrentLinkedQueue<int[]> colorsPool = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<float[]> uvsPool = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<int[]> lightsPool = new ConcurrentLinkedQueue<>();

    public EntityBlockTemplateMesh(
        EntityBlockRenderType type,
        int vertexCount,
        Vector4fc[] positions,
        int[] colors,
        float[] uvs,
        int[] lights,
        Vector3fc @Nullable [] normals
    ) {
        this.type = type;
        this.vertexCount = vertexCount;
        this.positions = positions;
        this.colors = colors;
        this.uvs = uvs;
        this.lights = lights;
        this.normals = normals;
    }

    public Vector4fc[] getLightPositions() {
        if (lightPositions == null) {
            Vector4fc[] data = new Vector4f[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                Vector4fc position = positions[i];
                data[i] = new Vector4f(
                    (position.x() - 0.5f) * 0.9375f + 0.5f,
                    (position.y() - 0.5f) * 0.9375f + 0.5f,
                    (position.z() - 0.5f) * 0.9375f + 0.5f,
                    1.0f
                );
            }
            lightPositions = data;
        }
        return lightPositions;
    }

    public int[] createColors() {
        int[] colors = colorsPool.poll();
        return colors == null ? new int[vertexCount] : colors;
    }

    public float[] createUVs() {
        float[] uvs = uvsPool.poll();
        return uvs == null ? new float[vertexCount << 1] : uvs;
    }

    public int[] createLights() {
        int[] lights = lightsPool.poll();
        return lights == null ? new int[vertexCount] : lights;
    }

    public void recycle(int[] colors, float[] uvs, int[] lights) {
        if (colors != this.colors) {
            colorsPool.offer(colors);
        }
        if (uvs != this.uvs) {
            uvsPool.offer(uvs);
        }
        if (lights != this.lights) {
            lightsPool.offer(lights);
        }
    }
}
