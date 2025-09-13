package com.zurrtum.create.client.catnip.impl.client.render;

import com.zurrtum.create.client.flywheel.lib.math.MatrixMath;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/common/src/lib/java/dev/engine_room/flywheel/lib/model/baked/TransformingVertexConsumer.java
public class TransformingVertexConsumer implements VertexConsumer {
    @UnknownNullability
    private VertexConsumer delegate;
    @UnknownNullability
    private MatrixStack poseStack;

    public void prepare(VertexConsumer delegate, MatrixStack poseStack) {
        this.delegate = delegate;
        this.poseStack = poseStack;
    }

    public void clear() {
        delegate = null;
        poseStack = null;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        Matrix4f matrix = poseStack.peek().getPositionMatrix();

        delegate.vertex(
            MatrixMath.transformPositionX(matrix, x, y, z),
            MatrixMath.transformPositionY(matrix, x, y, z),
            MatrixMath.transformPositionZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        delegate.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        Matrix3f matrix = poseStack.peek().getNormalMatrix();
        delegate.normal(
            MatrixMath.transformNormalX(matrix, x, y, z),
            MatrixMath.transformNormalY(matrix, x, y, z),
            MatrixMath.transformNormalZ(matrix, x, y, z)
        );
        return this;
    }
}
