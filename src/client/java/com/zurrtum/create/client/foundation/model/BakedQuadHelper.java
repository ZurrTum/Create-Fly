package com.zurrtum.create.client.foundation.model;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public final class BakedQuadHelper {

    public static final VertexFormat FORMAT = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    public static final int VERTEX_STRIDE = FORMAT.getVertexSize() / 4;

    public static final int X_OFFSET = 0;
    public static final int Y_OFFSET = 1;
    public static final int Z_OFFSET = 2;
    public static final int COLOR_OFFSET = 3;
    public static final int U_OFFSET = 4;
    public static final int V_OFFSET = 5;
    public static final int LIGHT_OFFSET = 6;
    public static final int NORMAL_OFFSET = 7;

    private BakedQuadHelper() {
    }

    public static BakedQuad clone(BakedQuad quad) {
        BakedQuad bakedQuad = new BakedQuad(
            Arrays.copyOf(quad.vertexData(), quad.vertexData().length),
            quad.tintIndex(),
            quad.face(),
            quad.sprite(),
            quad.shade(),
            quad.lightEmission()
        );
        if (NormalsBakedQuad.hasNormals(quad)) {
            NormalsBakedQuad.markNormals(bakedQuad);
        }
        return bakedQuad;
    }

    public static BakedQuad cloneWithCustomGeometry(BakedQuad quad, int[] vertexData) {
        BakedQuad bakedQuad = new BakedQuad(vertexData, quad.tintIndex(), quad.face(), quad.sprite(), quad.shade(), quad.lightEmission());
        if (NormalsBakedQuad.hasNormals(quad)) {
            NormalsBakedQuad.markNormals(bakedQuad);
        }
        return bakedQuad;
    }

    public static Vec3d getXYZ(int[] vertexData, int vertex) {
        float x = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + X_OFFSET]);
        float y = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + Y_OFFSET]);
        float z = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + Z_OFFSET]);
        return new Vec3d(x, y, z);
    }

    public static void setXYZ(int[] vertexData, int vertex, Vec3d xyz) {
        vertexData[vertex * VERTEX_STRIDE + X_OFFSET] = Float.floatToRawIntBits((float) xyz.x);
        vertexData[vertex * VERTEX_STRIDE + Y_OFFSET] = Float.floatToRawIntBits((float) xyz.y);
        vertexData[vertex * VERTEX_STRIDE + Z_OFFSET] = Float.floatToRawIntBits((float) xyz.z);
    }

    public static Vec3d getNormalXYZ(int[] vertexData, int vertex) {
        int data = vertexData[vertex * VERTEX_STRIDE + NORMAL_OFFSET];
        float x = (byte) (data >> 24 & 0xFF) / 127f;
        float y = (byte) (data >> 16 & 0xFF) / 127f;
        float z = (byte) (data >> 8 & 0xFF) / 127f;
        return new Vec3d(x, y, z);
    }

    public static void setNormalXYZ(int[] vertexData, int vertex, Vec3d xyz) {
        int x = Byte.toUnsignedInt((byte) (MathHelper.clamp(xyz.x, -1.0f, 1.0f) * 127));
        int y = Byte.toUnsignedInt((byte) (MathHelper.clamp(xyz.y, -1.0f, 1.0f) * 127));
        int z = Byte.toUnsignedInt((byte) (MathHelper.clamp(xyz.z, -1.0f, 1.0f) * 127));
        int data = (x << 24) | (y << 16) | (z << 8);
        vertexData[vertex * VERTEX_STRIDE + NORMAL_OFFSET] = data;
    }

    public static float getU(int[] vertexData, int vertex) {
        return Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + U_OFFSET]);
    }

    public static float getV(int[] vertexData, int vertex) {
        return Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + V_OFFSET]);
    }

    public static void setU(int[] vertexData, int vertex, float u) {
        vertexData[vertex * VERTEX_STRIDE + U_OFFSET] = Float.floatToRawIntBits(u);
    }

    public static void setV(int[] vertexData, int vertex, float v) {
        vertexData[vertex * VERTEX_STRIDE + V_OFFSET] = Float.floatToRawIntBits(v);
    }

}
