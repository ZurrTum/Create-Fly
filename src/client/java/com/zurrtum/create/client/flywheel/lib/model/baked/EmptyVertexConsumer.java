package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

class EmptyVertexConsumer implements VertexConsumer {
    public static final VertexConsumer INSTANCE = new EmptyVertexConsumer();

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer color(int color) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    @Override
    public void vertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
    }

    @Override
    public VertexConsumer color(float r, float g, float b, float a) {
        return this;
    }

    @Override
    public VertexConsumer light(int packedLightCoords) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int packedOverlayCoords) {
        return this;
    }

    @Override
    public void quad(MatrixStack.Entry pose, BakedQuad quad, float r, float g, float b, float a, int lightCoords, int overlayCoords) {
    }

    @Override
    public void quad(
        MatrixStack.Entry pose,
        BakedQuad quad,
        float[] brightness,
        float r,
        float g,
        float b,
        float a,
        int[] lightmapCoord,
        int overlayCoords, boolean colorize
    ) {
    }

    @Override
    public VertexConsumer vertex(Vector3f position) {
        return this;
    }

    @Override
    public VertexConsumer vertex(MatrixStack.Entry pose, Vector3f position) {
        return this;
    }

    @Override
    public VertexConsumer vertex(MatrixStack.Entry pose, float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer vertex(Matrix4f pose, float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer vertex(Matrix3x2f pose, float x, float y) {
        return this;
    }

    @Override
    public VertexConsumer normal(MatrixStack.Entry pose, Vector3f normal) {
        return this;
    }

    @Override
    public VertexConsumer normal(MatrixStack.Entry pose, float x, float y, float z) {
        return this;
    }
}
