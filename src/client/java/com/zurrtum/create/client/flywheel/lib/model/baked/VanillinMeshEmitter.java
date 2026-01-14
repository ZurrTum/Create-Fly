package com.zurrtum.create.client.flywheel.lib.model.baked;


import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;

public class VanillinMeshEmitter extends MeshEmitter implements VertexConsumer {
    private boolean useAo;

    VanillinMeshEmitter(MeshEmitterManager<VanillinMeshEmitter> manager, ByteBufferBuilderStack byteBufferBuilderStack, BlockRenderLayer renderType) {
        super(manager, byteBufferBuilderStack, renderType);
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
    }

    @Override
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        BufferBuilder bufferBuilder = getBuffer(quad.shade(), useAo);
        if (bufferBuilder != null) {
            bufferBuilder.quad(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
        }
    }

    @Override
    public void quad(
        MatrixStack.Entry pose,
        BakedQuad quad,
        float[] brightness,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lightmap,
        int packedOverlay,
        boolean readAlpha
    ) {
        BufferBuilder bufferBuilder = getBuffer(quad.shade(), useAo);
        if (bufferBuilder != null) {
            bufferBuilder.quad(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
        }
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int color) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }
}
