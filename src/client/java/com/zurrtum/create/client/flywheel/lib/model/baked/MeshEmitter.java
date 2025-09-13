package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.UnknownNullability;

class MeshEmitter implements VertexConsumer {
    private final BlockRenderLayer renderType;
    private final BufferAllocator byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    private BakedModelBufferer.ResultConsumer resultConsumer;
    private boolean currentShade;

    MeshEmitter(BlockRenderLayer renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new BufferAllocator(renderType.getBufferSize());
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
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        }

        currentShade = shade;
    }

    private void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
    }

    private void emit() {
        var data = bufferBuilder.endNullable();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
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
        prepareForGeometry(quad);
        bufferBuilder.quad(
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
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        prepareForGeometry(quad);
        bufferBuilder.quad(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
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
        prepareForGeometry(quad);
        bufferBuilder.quad(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
    }
}
