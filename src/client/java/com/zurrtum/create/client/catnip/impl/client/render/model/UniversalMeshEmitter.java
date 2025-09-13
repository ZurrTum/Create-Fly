package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.UnknownNullability;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
public class UniversalMeshEmitter implements VertexConsumer {
    @UnknownNullability
    private ShadeSeparatedBufferSource bufferSource;
    @UnknownNullability
    private BlockRenderLayer layer;

    public void prepare(ShadeSeparatedBufferSource bufferSource, BlockRenderLayer layer) {
        this.bufferSource = bufferSource;
        this.layer = layer;
    }

    public void clear() {
        bufferSource = null;
    }

    @Override
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        VertexConsumer buffer = bufferSource.getBuffer(layer, quad.shade());
        buffer.quad(pose, quad, red, green, blue, alpha, light, overlay);
    }

    public void quad(
        MatrixStack.Entry pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int light,
        int overlay,
        boolean readExistingColor
    ) {
        VertexConsumer buffer = bufferSource.getBuffer(layer, quad.shade());
        buffer.quad(
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
    public void quad(
        MatrixStack.Entry pose,
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
        VertexConsumer buffer = bufferSource.getBuffer(layer, quad.shade());
        buffer.quad(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }
}
