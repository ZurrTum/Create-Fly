package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

class EmptyVertexConsumer implements VertexConsumer {
    public static final VertexConsumer INSTANCE = new EmptyVertexConsumer();

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        return this;
    }

    @Override
    public void addVertex(
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        int overlayCoords,
        int lightCoords,
        float nx,
        float ny,
        float nz
    ) {
    }

    @Override
    public VertexConsumer setColor(float r, float g, float b, float a) {
        return this;
    }

    @Override
    public VertexConsumer setLight(int packedLightCoords) {
        return this;
    }

    @Override
    public VertexConsumer setOverlay(int packedOverlayCoords) {
        return this;
    }

    @Override
    public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
    }

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
    }

    @Override
    public VertexConsumer addVertex(Vector3fc position) {
        return this;
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, Vector3fc position) {
        return this;
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer addVertex(Matrix4fc pose, float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer addVertexWith2DPose(Matrix3x2fc pose, float x, float y) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose pose, Vector3fc normal) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
        return this;
    }
}
