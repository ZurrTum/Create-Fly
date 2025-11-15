package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.math.MatrixMath;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

class TransformingVertexConsumer implements VertexConsumer {
    private @UnknownNullability VertexConsumer delegate;
    private @UnknownNullability PoseStack poseStack;

    TransformingVertexConsumer() {
    }

    public void prepare(VertexConsumer delegate, PoseStack poseStack) {
        this.delegate = delegate;
        this.poseStack = poseStack;
    }

    public void clear() {
        this.delegate = null;
        this.poseStack = null;
    }

    public VertexConsumer addVertex(float x, float y, float z) {
        Matrix4f matrix = this.poseStack.last().pose();
        this.delegate.addVertex(
            MatrixMath.transformPositionX(matrix, x, y, z),
            MatrixMath.transformPositionY(matrix, x, y, z),
            MatrixMath.transformPositionZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        this.delegate.setColor(color);
        return this;
    }

    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, alpha);
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    public VertexConsumer setNormal(float x, float y, float z) {
        Matrix3f matrix = this.poseStack.last().normal();
        this.delegate.setNormal(
            MatrixMath.transformNormalX(matrix, x, y, z),
            MatrixMath.transformNormalY(matrix, x, y, z),
            MatrixMath.transformNormalZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
