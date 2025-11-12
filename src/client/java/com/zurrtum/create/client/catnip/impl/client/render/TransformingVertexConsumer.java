package com.zurrtum.create.client.catnip.impl.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.math.MatrixMath;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/common/src/lib/java/dev/engine_room/flywheel/lib/model/baked/TransformingVertexConsumer.java
public class TransformingVertexConsumer implements VertexConsumer {
    @UnknownNullability
    private VertexConsumer delegate;
    @UnknownNullability
    private PoseStack poseStack;

    public void prepare(VertexConsumer delegate, PoseStack poseStack) {
        this.delegate = delegate;
        this.poseStack = poseStack;
    }

    public void clear() {
        delegate = null;
        poseStack = null;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        Matrix4f matrix = poseStack.last().pose();

        delegate.addVertex(
            MatrixMath.transformPositionX(matrix, x, y, z),
            MatrixMath.transformPositionY(matrix, x, y, z),
            MatrixMath.transformPositionZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        Matrix3f matrix = poseStack.last().normal();
        delegate.setNormal(
            MatrixMath.transformNormalX(matrix, x, y, z),
            MatrixMath.transformNormalY(matrix, x, y, z),
            MatrixMath.transformNormalZ(matrix, x, y, z)
        );
        return this;
    }
}
