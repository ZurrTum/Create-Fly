package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Map;

public class CubeParticleSubmittable implements SubmitNodeCollector.ParticleGroupRenderer, ParticleGroupRenderState {
    public static final Vector3f[] CUBE = {
        // TOP
        new Vector3f(-1, -1, 1), new Vector3f(-1, -1, -1), new Vector3f(1, -1, -1), new Vector3f(1, -1, 1),

        // BOTTOM
        new Vector3f(1, 1, 1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, 1, 1),

        // FRONT
        new Vector3f(1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, -1, -1), new Vector3f(-1, 1, -1),

        // BACK
        new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, 1, 1),

        // LEFT
        new Vector3f(1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(1, -1, -1), new Vector3f(1, 1, -1),

        // RIGHT
        new Vector3f(-1, 1, -1), new Vector3f(-1, -1, -1), new Vector3f(-1, -1, 1), new Vector3f(-1, 1, 1)};

    private final Vertices vertices = new Vertices();
    private int particles;

    public void render(float x, float y, float z, float scale, int color) {
        vertices.vertex(x, y, z, scale, color);
        particles++;
    }

    @Override
    public void clear() {
        vertices.reset();
        particles = 0;
    }

    @Override
    public QuadParticleRenderState.PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache cache) {
        int i = particles * 24;
        try (ByteBufferBuilder bufferAllocator = ByteBufferBuilder.exactlySized(i * DefaultVertexFormat.PARTICLE.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            vertices.render((x, y, z, scale, color) -> drawFace(bufferBuilder, x, y, z, scale, color));
            QuadParticleRenderState.PreparedLayer layer = new QuadParticleRenderState.PreparedLayer(0, vertices.nextVertexIndex() * 36);
            MeshData builtBuffer = bufferBuilder.build();
            if (builtBuffer != null) {
                cache.write(builtBuffer.vertexBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(builtBuffer.drawState().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
                return new QuadParticleRenderState.PreparedBuffers(
                    builtBuffer.drawState().indexCount(),
                    gpuBufferSlice,
                    Map.of(CubeParticleRenderer.RENDER_TYPE, layer)
                );
            }
        }
        return null;
    }

    private void drawFace(VertexConsumer buffer, float x, float y, float z, float scale, int color) {
        int light = LightCoordsUtil.FULL_BRIGHT;
        Vector3f vec = new Vector3f();
        for (int i = 0; i < 6; i++) {
            // 6 faces to a cube
            for (int j = 0; j < 4; j++) {
                CUBE[i * 4 + j].mul(scale, vec).add(x, y, z);
                buffer.addVertex(vec.x, vec.y, vec.z).setUv((float) j / 2, j % 2).setColor(color).setLight(light);
            }
        }
    }

    @Override
    public void render(
        QuadParticleRenderState.PreparedBuffers buffers,
        ParticleFeatureRenderer.ParticleBufferCache cache,
        RenderPass renderPass,
        TextureManager manager,
        boolean translucent
    ) {
        if (translucent) {
            return;
        }
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        renderPass.setVertexBuffer(0, cache.get());
        renderPass.setIndexBuffer(shapeIndexBuffer.getBuffer(buffers.indexCount()), shapeIndexBuffer.type());
        renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms());
        for (Map.Entry<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer> entry : buffers.layers().entrySet()) {
            renderPass.setPipeline(entry.getKey().pipeline());
            AbstractTexture texture = manager.getTexture(entry.getKey().textureAtlasLocation());
            renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            renderPass.drawIndexed(0, 0, entry.getValue().indexCount(), 1);
        }
    }

    @Override
    public void submit(SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        if (particles > 0) {
            queue.submitParticleGroup(this);
        }
    }

    @FunctionalInterface
    public interface Consumer {
        void consume(float x, float y, float z, float scale, int color);
    }

    public static class Vertices {
        private int maxVertices = 1024;
        private float[] floatData = new float[12288];
        private int[] intData = new int[2048];
        private int nextVertexIndex;

        public void vertex(float x, float y, float z, float scale, int color) {
            if (nextVertexIndex >= maxVertices) {
                increaseCapacity();
            }
            int i = this.nextVertexIndex * 4;
            floatData[i++] = x;
            floatData[i++] = y;
            floatData[i++] = z;
            floatData[i] = scale;
            intData[nextVertexIndex * 2] = color;
            nextVertexIndex++;
        }

        public void render(Consumer consumer) {
            for (int i = 0; i < this.nextVertexIndex; i++) {
                int j = i * 4;
                consumer.consume(floatData[j++], floatData[j++], floatData[j++], floatData[j], intData[i]);
            }
        }

        public void reset() {
            nextVertexIndex = 0;
        }

        private void increaseCapacity() {
            maxVertices *= 2;
            floatData = Arrays.copyOf(floatData, maxVertices * 4);
            intData = Arrays.copyOf(intData, maxVertices);
        }

        public int nextVertexIndex() {
            return nextVertexIndex;
        }
    }
}
