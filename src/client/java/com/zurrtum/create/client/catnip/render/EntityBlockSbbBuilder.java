package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.AbstractMeshBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitterOutput;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

public class EntityBlockSbbBuilder implements BufferEmitter {
    private static final int INITIAL_CAPACITY = 256;
    protected final TemplateMeshBuffer[] buffers;
    protected final LightTemplateMeshBuffer[] lightBuffers;
    protected final DirectionTemplateMeshBuffer[] directionBuffers;
    protected Vector3f normal;
    private @Nullable Object cache;

    public EntityBlockSbbBuilder() {
        buffers = new TemplateMeshBuffer[]{
            new TemplateMeshBuffer(EntityBlockRenderType.SOLID, INITIAL_CAPACITY),
            new TemplateMeshBuffer(EntityBlockRenderType.CUTOUT, INITIAL_CAPACITY),
            new TemplateMeshBuffer(EntityBlockRenderType.TRANSLUCENT, INITIAL_CAPACITY)
        };
        lightBuffers = new LightTemplateMeshBuffer[]{
            new LightTemplateMeshBuffer(EntityBlockRenderType.SOLID_LIGHT, INITIAL_CAPACITY),
            new LightTemplateMeshBuffer(EntityBlockRenderType.CUTOUT_LIGHT, INITIAL_CAPACITY),
            new LightTemplateMeshBuffer(EntityBlockRenderType.TRANSLUCENT_LIGHT, INITIAL_CAPACITY)
        };
        directionBuffers = new DirectionTemplateMeshBuffer[]{
            new DirectionTemplateMeshBuffer(buffers[0]),
            new DirectionTemplateMeshBuffer(buffers[1]),
            new DirectionTemplateMeshBuffer(buffers[2])
        };
        normal = new Vector3f();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public EntityBlockSbbBuilder(EntityBlockSbbBuilder builder) {
        buffers = builder.buffers;
        lightBuffers = builder.lightBuffers;
        directionBuffers = builder.directionBuffers;
        normal = builder.normal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BufferEmitterOutput, Emitter> Emitter getOrCreateEmitter(Function<T, Emitter> func) {
        if (cache != null) {
            return (Emitter) cache;
        }
        Emitter emitter = func.apply((T) this);
        cache = emitter;
        return emitter;
    }

    @Override
    public VertexConsumer getBuffer(@Nullable Direction shadeDirectionOverride, ChunkSectionLayer layer) {
        int index = layer.ordinal();
        if (shadeDirectionOverride == null) {
            return buffers[index];
        }
        if (shadeDirectionOverride == Direction.UP) {
            return lightBuffers[index];
        }
        shadeDirectionOverride(shadeDirectionOverride, normal);
        return directionBuffers[index];
    }

    protected void shadeDirectionOverride(Direction direction, Vector3f normal) {
        normal.set(direction.getUnitVec3f());
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        getBuffer(info.shadeDirectionOverride(), info.layer()).putBlockBakedQuad(x, y, z, quad, instance);
    }

    public SuperByteBuffer build() {
        int index = 0;
        for (TemplateMeshBuffer buffer : buffers) {
            if (buffer.hasVertices()) {
                index++;
            }
        }
        for (LightTemplateMeshBuffer buffer : lightBuffers) {
            if (buffer.hasVertices()) {
                index++;
            }
        }
        if (index == 0) {
            return EmptySuperByteBuffer.INSTANCE;
        }
        EntityBlockTemplateMesh[] templates = new EntityBlockTemplateMesh[index];
        index = 0;
        for (int i = 0; i < 3; i++) {
            EntityBlockTemplateMesh template = buffers[i].build();
            if (template != null) {
                templates[index++] = template;
            }
        }
        for (int i = 0; i < 3; i++) {
            EntityBlockTemplateMesh template = lightBuffers[i].build();
            if (template != null) {
                templates[index++] = template;
            }
        }
        return new SuperByteBuffer(templates);
    }

    protected static class TemplateMeshBuffer extends AbstractMeshBuffer {
        public final EntityBlockRenderType type;
        public int capacity;
        public int index;
        public Vector4fc[] positions;
        public int[] colors;
        public float[] uvs;
        public int[] lights;
        public Vector3fc[] normals;

        public TemplateMeshBuffer(EntityBlockRenderType type, int capacity) {
            this.type = type;
            this.capacity = capacity;
            positions = new Vector4fc[capacity];
            colors = new int[capacity];
            uvs = new float[capacity << 1];
            lights = new int[capacity];
            normals = new Vector3fc[capacity];
        }

        public boolean hasVertices() {
            return index != 0;
        }

        @Nullable
        public EntityBlockTemplateMesh build() {
            if (index == 0) {
                return null;
            }
            int index = this.index;
            this.index = 0;
            Vector4fc[] positions = new Vector4fc[index];
            System.arraycopy(this.positions, 0, positions, 0, index);
            int[] colors = new int[index];
            System.arraycopy(this.colors, 0, colors, 0, index);
            float[] uvs = new float[index << 1];
            System.arraycopy(this.uvs, 0, uvs, 0, index << 1);
            int[] lights = new int[index];
            System.arraycopy(this.lights, 0, lights, 0, index);
            Vector3fc[] normals = new Vector3fc[index];
            System.arraycopy(this.normals, 0, normals, 0, index);
            Arrays.fill(this.positions, 0, index, null);
            Arrays.fill(this.normals, 0, index, null);
            return new EntityBlockTemplateMesh(type, index, positions, colors, uvs, lights, normals);
        }

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            if (index == capacity) {
                capacity <<= 1;
                Vector4fc[] positions = this.positions;
                this.positions = new Vector4fc[capacity];
                System.arraycopy(positions, 0, this.positions, 0, index);
                int[] colors = this.colors;
                this.colors = new int[capacity];
                System.arraycopy(colors, 0, this.colors, 0, index);
                float[] uvs = this.uvs;
                this.uvs = new float[capacity << 1];
                System.arraycopy(uvs, 0, this.uvs, 0, capacity);
                int[] lights = this.lights;
                this.lights = new int[capacity];
                System.arraycopy(lights, 0, this.lights, 0, index);
                Vector3fc[] normals = this.normals;
                this.normals = new Vector3fc[capacity];
                System.arraycopy(normals, 0, this.normals, 0, index);
            }
            positions[index] = new Vector4f(x, y, z, 1);
            colors[index] = color;
            uvs[index << 1] = u;
            uvs[(index << 1) + 1] = v;
            lights[index] = lightCoords;
            normals[index] = new Vector3f(nx, ny, nz);
            index++;
        }
    }

    protected static class LightTemplateMeshBuffer extends AbstractMeshBuffer {
        public final EntityBlockRenderType type;
        public int capacity;
        public int index;
        public Vector4fc[] positions;
        public int[] colors;
        public float[] uvs;
        public int[] lights;

        public LightTemplateMeshBuffer(EntityBlockRenderType type, int capacity) {
            this.type = type;
            this.capacity = capacity;
            positions = new Vector4fc[capacity];
            colors = new int[capacity];
            uvs = new float[capacity << 1];
            lights = new int[capacity];
        }

        public boolean hasVertices() {
            return index != 0;
        }

        @Nullable
        public EntityBlockTemplateMesh build() {
            if (index == 0) {
                return null;
            }
            int index = this.index;
            this.index = 0;
            Vector4fc[] positions = new Vector4fc[index];
            System.arraycopy(this.positions, 0, positions, 0, index);
            int[] colors = new int[index];
            System.arraycopy(this.colors, 0, colors, 0, index);
            float[] uvs = new float[index << 1];
            System.arraycopy(this.uvs, 0, uvs, 0, index << 1);
            int[] lights = new int[index];
            System.arraycopy(this.lights, 0, lights, 0, index);
            Arrays.fill(this.positions, 0, index, null);
            return new EntityBlockTemplateMesh(type, index, positions, colors, uvs, lights, null);
        }

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            if (index == capacity) {
                capacity <<= 1;
                Vector4fc[] positions = this.positions;
                this.positions = new Vector4fc[capacity];
                System.arraycopy(positions, 0, this.positions, 0, index);
                int[] colors = this.colors;
                this.colors = new int[capacity];
                System.arraycopy(colors, 0, this.colors, 0, index);
                float[] uvs = this.uvs;
                this.uvs = new float[capacity << 1];
                System.arraycopy(uvs, 0, this.uvs, 0, capacity);
                int[] lights = this.lights;
                this.lights = new int[capacity];
                System.arraycopy(lights, 0, this.lights, 0, index);
            }
            positions[index] = new Vector4f(x, y, z, 1);
            colors[index] = color;
            uvs[index << 1] = u;
            uvs[(index << 1) + 1] = v;
            lights[index] = lightCoords;
            index++;
        }
    }

    protected class DirectionTemplateMeshBuffer extends AbstractMeshBuffer {
        private final TemplateMeshBuffer buffer;

        public DirectionTemplateMeshBuffer(TemplateMeshBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            buffer.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, normal.x(), normal.y(), normal.z());
        }
    }
}
