package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;

@Deprecated(forRemoval = true)
public class ShadedBlockSbbBuilder implements VertexConsumer {
    protected static final BufferAllocator BYTE_BUFFER_BUILDER = new BufferAllocator(512);
    protected BufferBuilder bufferBuilder;
    protected final IntList shadeSwapVertices = new IntArrayList();
    protected boolean currentShade;
    protected boolean invertFakeNormal;

    public static ShadedBlockSbbBuilder create() {
        return new ShadedBlockSbbBuilder();
    }

    public static ShadedBlockSbbBuilder createForPonder() {
        ShadedBlockSbbBuilder builder = new ShadedBlockSbbBuilder();
        builder.invertFakeNormal = true;
        return builder;
    }

    public void begin() {
        bufferBuilder = new BufferBuilder(BYTE_BUFFER_BUILDER, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        shadeSwapVertices.clear();
        currentShade = true;
    }

    public SuperByteBuffer end() {
        BuiltBuffer data = bufferBuilder.endNullable();
        TemplateMesh mesh;

        if (data != null) {
            mesh = new MutableTemplateMesh(data).toImmutable();
            data.close();
        } else {
            mesh = new TemplateMesh(0);
        }

        return new ShadeSeparatingSuperByteBuffer(mesh, shadeSwapVertices.toIntArray(), invertFakeNormal);
    }

    public BufferBuilder unwrap(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (shade != currentShade) {
            shadeSwapVertices.add(bufferBuilder.vertexCount);
            currentShade = shade;
        }
    }

    protected void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
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
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer light(int u, int v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }
}
