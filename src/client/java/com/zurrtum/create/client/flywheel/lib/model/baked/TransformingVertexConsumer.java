package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.lib.math.MatrixMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

class TransformingVertexConsumer implements VertexConsumer {
    private @UnknownNullability VertexConsumer delegate;
    private @UnknownNullability MatrixStack poseStack;

    TransformingVertexConsumer() {
    }

    public void prepare(VertexConsumer delegate, MatrixStack poseStack) {
        this.delegate = delegate;
        this.poseStack = poseStack;
    }

    public void clear() {
        this.delegate = null;
        this.poseStack = null;
    }

    public VertexConsumer vertex(float x, float y, float z) {
        Matrix4f matrix = this.poseStack.peek().getPositionMatrix();
        this.delegate.vertex(
            MatrixMath.transformPositionX(matrix, x, y, z),
            MatrixMath.transformPositionY(matrix, x, y, z),
            MatrixMath.transformPositionZ(matrix, x, y, z)
        );
        return this;
    }

    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.delegate.color(red, green, blue, alpha);
        return this;
    }

    public VertexConsumer texture(float u, float v) {
        this.delegate.texture(u, v);
        return this;
    }

    public VertexConsumer overlay(int u, int v) {
        this.delegate.overlay(u, v);
        return this;
    }

    public VertexConsumer light(int u, int v) {
        this.delegate.light(u, v);
        return this;
    }

    public VertexConsumer normal(float x, float y, float z) {
        Matrix3f matrix = this.poseStack.peek().getNormalMatrix();
        this.delegate.normal(
            MatrixMath.transformNormalX(matrix, x, y, z),
            MatrixMath.transformNormalY(matrix, x, y, z),
            MatrixMath.transformNormalZ(matrix, x, y, z)
        );
        return this;
    }
}
