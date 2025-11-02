package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.LayeredCustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Map;

public class CubeParticleSubmittable implements OrderedRenderCommandQueue.LayeredCustom, Submittable {
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
    public void onFrameEnd() {
        vertices.reset();
        particles = 0;
    }

    @Override
    public BillboardParticleSubmittable.Buffers submit(LayeredCustomCommandRenderer.VerticesCache cache) {
        int i = particles * 24;
        try (BufferAllocator bufferAllocator = BufferAllocator.fixedSized(i * VertexFormats.POSITION_TEXTURE_COLOR_LIGHT.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            vertices.render((x, y, z, scale, color) -> drawFace(bufferBuilder, x, y, z, scale, color));
            BillboardParticleSubmittable.Layer layer = new BillboardParticleSubmittable.Layer(0, vertices.nextVertexIndex() * 36);
            BuiltBuffer builtBuffer = bufferBuilder.endNullable();
            if (builtBuffer != null) {
                cache.write(builtBuffer.getBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS).getIndexBuffer(builtBuffer.getDrawParameters().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(
                    RenderSystem.getModelViewMatrix(),
                    new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                    new Vector3f(),
                    RenderSystem.getTextureMatrix(),
                    RenderSystem.getShaderLineWidth()
                );
                return new BillboardParticleSubmittable.Buffers(
                    builtBuffer.getDrawParameters().indexCount(),
                    gpuBufferSlice,
                    Map.of(CubeParticleRenderer.RENDER_TYPE, layer)
                );
            }
        }
        return null;
    }

    private void drawFace(VertexConsumer buffer, float x, float y, float z, float scale, int color) {
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        Vector3f vec = new Vector3f();
        for (int i = 0; i < 6; i++) {
            // 6 faces to a cube
            for (int j = 0; j < 4; j++) {
                CUBE[i * 4 + j].mul(scale, vec).add(x, y, z);
                buffer.vertex(vec.x, vec.y, vec.z).texture((float) j / 2, j % 2).color(color).light(light);
            }
        }
    }

    @Override
    public void render(
        BillboardParticleSubmittable.Buffers buffers,
        LayeredCustomCommandRenderer.VerticesCache cache,
        RenderPass renderPass,
        TextureManager manager,
        boolean translucent
    ) {
        if (translucent) {
            return;
        }
        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        renderPass.setVertexBuffer(0, cache.get());
        renderPass.setIndexBuffer(shapeIndexBuffer.getIndexBuffer(buffers.indexCount()), shapeIndexBuffer.getIndexType());
        renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms());
        for (Map.Entry<BillboardParticle.RenderType, BillboardParticleSubmittable.Layer> entry : buffers.layers().entrySet()) {
            renderPass.setPipeline(entry.getKey().pipeline());
            renderPass.bindSampler("Sampler0", manager.getTexture(entry.getKey().textureAtlasLocation()).getGlTextureView());
            renderPass.drawIndexed(0, 0, entry.getValue().indexCount(), 1);
        }
    }

    @Override
    public void submit(OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        if (particles > 0) {
            queue.submitCustom(this);
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
