package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.UnknownNullability;

class MeshEmitter implements VertexConsumer {
    private final ChunkSectionLayer renderType;
    private final ByteBufferBuilder byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    private BakedModelBufferer.ResultConsumer resultConsumer;
    private boolean currentShade;

    MeshEmitter(ChunkSectionLayer renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new ByteBufferBuilder(renderType.bufferSize());
    }

    public void prepare(BakedModelBufferer.ResultConsumer resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    public void end() {
        if (bufferBuilder != null) {
            emit();
        }
        resultConsumer = null;
    }

    public BufferBuilder unwrap(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (bufferBuilder == null) {
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        currentShade = shade;
    }

    private void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
    }

    private void emit() {
        var data = bufferBuilder.build();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
    }

    public void quad(
        PoseStack.Pose pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int light,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(
            pose,
            quad,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            red,
            green,
            blue,
            alpha,
            new int[]{light, light, light, light},
            overlay,
            readExistingColor
        );
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int packedLight,
        int packedOverlay
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        float[] brightnesses,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lights,
        int overlay,
        boolean readExistingColor
    ) {
        prepareForGeometry(quad);
        bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }
}
