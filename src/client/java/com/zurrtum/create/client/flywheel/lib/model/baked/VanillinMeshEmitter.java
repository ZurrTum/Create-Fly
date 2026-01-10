package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class VanillinMeshEmitter extends MeshEmitter implements VertexConsumer {
    private boolean useAo;

    VanillinMeshEmitter(
        MeshEmitterManager<VanillinMeshEmitter> manager,
        ByteBufferBuilderStack byteBufferBuilderStack,
        ChunkSectionLayer renderType
    ) {
        super(manager, byteBufferBuilderStack, renderType);
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
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
        BufferBuilder bufferBuilder = getBuffer(quad.shade(), useAo);
        if (bufferBuilder != null) {
            bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
        }
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        float[] brightness,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lightmap,
        int packedOverlay
    ) {
        BufferBuilder bufferBuilder = getBuffer(quad.shade(), useAo);
        if (bufferBuilder != null) {
            bufferBuilder.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay);
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int color) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        throw new UnsupportedOperationException("VanillinMeshEmitter only supports putBulkData!");
    }
}
