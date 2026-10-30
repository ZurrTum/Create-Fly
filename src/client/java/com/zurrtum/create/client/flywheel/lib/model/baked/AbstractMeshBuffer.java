package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class AbstractMeshBuffer implements VertexConsumer {
    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setColor(int color) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        throw new UnsupportedOperationException("only supports addVertex!");
    }
}
