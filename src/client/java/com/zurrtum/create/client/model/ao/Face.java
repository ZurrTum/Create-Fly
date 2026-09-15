package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class Face {
    public static final int LIGHT_FACE_FLAG = 1;
    public static final int CUBIC_FLAG_0 = 0;
    public static final int CUBIC_FLAG_1 = 2;
    public static final int CUBIC_FLAG_2 = 4;
    public static final int CUBIC_FLAG_3 = 6;
    public static final int CUBIC_FLAG = 1 << CUBIC_FLAG_0 | 1 << CUBIC_FLAG_1 | 1 << CUBIC_FLAG_2 | 1 << CUBIC_FLAG_3;
    public static final int PARTIAL_FLAG = 8;
    public static final float EPS_MIN = 0.0001f;
    public static final float EPS_MAX = 1.0f - EPS_MIN;

    public static boolean isIrregular(float w0, float w1, float w2, float w3) {
        return Math.abs(w0 - w1) >= 1.0E-5F | Math.abs(w0 - w2) >= 1.0E-5F | Math.abs(w0 - w3) >= 1.0E-5F;
    }

    public static float clamp(float value) {
        return Math.min(1f, Math.max(value, 0f));
    }

    private static float lerp(float a, float b, float w) {
        return Math.fma(b - a, w, a);
    }

    private static int lerp(int a, int b, float w) {
        return (int) Math.fma(b - a, w, a);
    }

    private static int light0(AoFaceData faceData) {
        return faceData.s0 << 16 | faceData.b0;
    }

    private static int light1(AoFaceData faceData) {
        return faceData.s1 << 16 | faceData.b1;
    }

    private static int light2(AoFaceData faceData) {
        return faceData.s2 << 16 | faceData.b2;
    }

    private static int light3(AoFaceData faceData) {
        return faceData.s3 << 16 | faceData.b3;
    }

    private static int light0(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s0, f1.s0, w) << 16 | lerp(f0.b0, f1.b0, w);
    }

    private static int light1(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s1, f1.s1, w) << 16 | lerp(f0.b1, f1.b1, w);
    }

    private static int light2(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s2, f1.s2, w) << 16 | lerp(f0.b2, f1.b2, w);
    }

    private static int light3(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s3, f1.s3, w) << 16 | lerp(f0.b3, f1.b3, w);
    }

    private static float a0(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.a0, f1.a0, w);
    }

    private static float a1(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.a1, f1.a1, w);
    }

    private static float a2(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.a2, f1.a2, w);
    }

    private static float a3(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.a3, f1.a3, w);
    }

    private static int b0(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.b0, f1.b0, w);
    }

    private static int b1(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.b1, f1.b1, w);
    }

    private static int b2(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.b2, f1.b2, w);
    }

    private static int b3(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.b3, f1.b3, w);
    }

    private static int s0(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s0, f1.s0, w);
    }

    private static int s1(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s1, f1.s1, w);
    }

    private static int s2(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s2, f1.s2, w);
    }

    private static int s3(AoFaceData f0, AoFaceData f1, float w) {
        return lerp(f0.s3, f1.s3, w);
    }

    private static float weightedAo(float a0, float a1, float a2, float a3, float w0, float w1, float w2, float w3) {
        return a0 * w0 + a1 * w1 + a2 * w2 + a3 * w3;
    }

    private static float weightedAo(float a0, float a1, float w0, float w1) {
        return (a0 * w0 + a1 * w1 + Math.max(a0, a1)) * 0.5f;
    }

    private static float weightedAo(float a0, float a1, float a2, float w0, float w1, float w2) {
        return (a0 * w0 + a1 * w1 + a2 * w2 + Math.max(Math.max(a0, a1), a2)) * 0.5f;
    }

    public static float weightedAo(AoFaceData faceData, float w0, float w1, float w2, float w3) {
        return weightedAo(faceData.a0, faceData.a1, faceData.a2, faceData.a3, w0, w1, w2, w3);
    }

    public static float weightedAo(AoFaceData f0, AoFaceData f1, float w, float w0, float w1, float w2, float w3) {
        float a0 = a0(f0, f1, w), a1 = a1(f0, f1, w), a2 = a2(f0, f1, w), a3 = a3(f0, f1, w);
        return weightedAo(a0, a1, a2, a3, w0, w1, w2, w3);
    }

    private static int weightedLight(int v0, int v1, int v2, int v3, float w0, float w1, float w2, float w3) {
        return (int) (v0 * w0 + v1 * w1 + v2 * w2 + v3 * w3);
    }

    private static int weightedLight(int v0, int v1, float w0, float w1) {
        return (int) ((v0 * w0 + v1 * w1 + Math.max(v0, v1)) * 0.5f);
    }

    private static int weightedLight(int v0, int v1, int v2, float w0, float w1, float w2) {
        return (int) ((v0 * w0 + v1 * w1 + v2 * w2 + Math.max(Math.max(v0, v1), v2)) * 0.5f);
    }

    private static int weightedLight(
        int s0,
        int s1,
        int s2,
        int s3,
        int b0,
        int b1,
        int b2,
        int b3,
        float w0,
        float w1,
        float w2,
        float w3
    ) {
        return weightedLight(s0, s1, s2, s3, w0, w1, w2, w3) << 16 | weightedLight(b0, b1, b2, b3, w0, w1, w2, w3);
    }

    private static int weightedLight(int s0, int s1, int b0, int b1, float w0, float w1) {
        return weightedLight(s0, s1, w0, w1) << 16 | weightedLight(b0, b1, w0, w1);
    }

    private static int weightedLight(int s0, int s1, int s2, int b0, int b1, int b2, float w0, float w1, float w2) {
        return weightedLight(s0, s1, s2, w0, w1, w2) << 16 | weightedLight(b0, b1, b2, w0, w1, w2);
    }

    public static int weightedBlockLight(AoFaceData faceData, float w0, float w1, float w2, float w3) {
        return weightedLight(faceData.b0, faceData.b1, faceData.b2, faceData.b3, w0, w1, w2, w3);
    }

    public static int weightedBlockLight(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float w0,
        float w1,
        float w2,
        float w3
    ) {
        int b0 = b0(f0, f1, w), b1 = b1(f0, f1, w), b2 = b2(f0, f1, w), b3 = b3(f0, f1, w);
        return weightedLight(b0, b1, b2, b3, w0, w1, w2, w3);
    }

    public static int weightedSkyLight(AoFaceData faceData, float w0, float w1, float w2, float w3) {
        return weightedLight(faceData.s0, faceData.s1, faceData.s2, faceData.s3, w0, w1, w2, w3);
    }

    public static int weightedSkyLight(AoFaceData f0, AoFaceData f1, float w, float w0, float w1, float w2, float w3) {
        int s0 = s0(f0, f1, w), s1 = s1(f0, f1, w), s2 = s2(f0, f1, w), s3 = s3(f0, f1, w);
        return weightedLight(s0, s1, s2, s3, w0, w1, w2, w3);
    }

    public static void write(int i, QuadInstance output, float ao, int light) {
        output.setColor(i, ARGB.gray(ao));
        output.setLightCoords(i, light);
    }

    private static void fullFace(AoFaceData faceData, int i0, int i1, int i2, int i3, QuadInstance output) {
        write(i0, output, faceData.a0, light0(faceData));
        write(i1, output, faceData.a1, light1(faceData));
        write(i2, output, faceData.a2, light2(faceData));
        write(i3, output, faceData.a3, light3(faceData));
    }

    public static void fullFace0(AoFaceData faceData, QuadInstance output) {
        fullFace(faceData, 0, 1, 2, 3, output);
    }

    public static void fullFace1(AoFaceData faceData, QuadInstance output) {
        fullFace(faceData, 1, 2, 3, 0, output);
    }

    public static void fullFace2(AoFaceData faceData, QuadInstance output) {
        fullFace(faceData, 2, 3, 0, 1, output);
    }

    public static void fullFace3(AoFaceData faceData, QuadInstance output) {
        fullFace(faceData, 3, 0, 1, 2, output);
    }

    private static void fullFace(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        int i0,
        int i1,
        int i2,
        int i3,
        QuadInstance output
    ) {
        write(i0, output, a0(f0, f1, w), light0(f0, f1, w));
        write(i1, output, a1(f0, f1, w), light1(f0, f1, w));
        write(i2, output, a2(f0, f1, w), light2(f0, f1, w));
        write(i3, output, a3(f0, f1, w), light3(f0, f1, w));
    }

    public static void fullFace0(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, clamp(w), 0, 1, 2, 3, output);
    }

    public static void fullFlipFace0(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, 1 - clamp(w), 0, 1, 2, 3, output);
    }

    public static void fullFace1(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, clamp(w), 1, 2, 3, 0, output);
    }

    public static void fullFlipFace1(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, 1 - clamp(w), 1, 2, 3, 0, output);
    }

    public static void fullFace2(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, clamp(w), 2, 3, 0, 1, output);
    }

    public static void fullFlipFace2(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, 1 - clamp(w), 2, 3, 0, 1, output);
    }

    public static void fullFace3(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, clamp(w), 3, 0, 1, 2, output);
    }

    public static void fullFlipFace3(AoFaceData f0, AoFaceData f1, float w, QuadInstance output) {
        fullFace(f0, f1, 1 - clamp(w), 3, 0, 1, 2, output);
    }

    public static void write(
        int i,
        float a0,
        float a1,
        int s0,
        int s1,
        int b0,
        int b1,
        float w0,
        float w1,
        QuadInstance output
    ) {
        write(i, output, weightedAo(a0, a1, w0, w1), weightedLight(s0, s1, b0, b1, w0, w1));
    }

    public static void write(
        int i,
        float a0,
        float a1,
        float a2,
        int s0,
        int s1,
        int s2,
        int b0,
        int b1,
        int b2,
        float w0,
        float w1,
        float w2,
        QuadInstance output
    ) {
        write(i, output, weightedAo(a0, a1, a2, w0, w1, w2), weightedLight(s0, s1, s2, b0, b1, b2, w0, w1, w2));
    }

    private static void write(
        int i,
        float a0,
        float a1,
        float a2,
        float a3,
        int s0,
        int s1,
        int s2,
        int s3,
        int b0,
        int b1,
        int b2,
        int b3,
        float u0,
        float v0,
        float u1,
        float v1,
        QuadInstance output
    ) {
        float w0 = u1 * v0, w1 = u1 * v1, w2 = u0 * v1, w3 = u0 * v0;
        float ao = weightedAo(a0, a1, a2, a3, w0, w1, w2, w3);
        int light = weightedLight(s0, s1, s2, s3, b0, b1, b2, b3, w0, w1, w2, w3);
        write(i, output, ao, light);
    }

    public static void write(int i, AoFaceData faceData, float u0, float v0, float u1, float v1, QuadInstance output) {
        float a0 = faceData.a0, a1 = faceData.a1, a2 = faceData.a2, a3 = faceData.a3;
        int s0 = faceData.s0, s1 = faceData.s1, s2 = faceData.s2, s3 = faceData.s3;
        int b0 = faceData.b0, b1 = faceData.b1, b2 = faceData.b2, b3 = faceData.b3;
        write(i, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u0, v0, u1, v1, output);
    }

    public static void write(
        int i,
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u0,
        float v0,
        float u1,
        float v1,
        QuadInstance output
    ) {
        float a0 = a0(f0, f1, w), a1 = a1(f0, f1, w), a2 = a2(f0, f1, w), a3 = a3(f0, f1, w);
        int s0 = s0(f0, f1, w), s1 = s1(f0, f1, w), s2 = s2(f0, f1, w), s3 = s3(f0, f1, w);
        int b0 = b0(f0, f1, w), b1 = b1(f0, f1, w), b2 = b2(f0, f1, w), b3 = b3(f0, f1, w);
        write(i, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u0, v0, u1, v1, output);
    }

    private static void partialFace(
        int i,
        float a0,
        float a1,
        float a2,
        float a3,
        int s0,
        int s1,
        int s2,
        int s3,
        int b0,
        int b1,
        int b2,
        int b3,
        float x0,
        float y0,
        float x2,
        float y2,
        float x4,
        float y4,
        float x6,
        float y6,
        QuadInstance output
    ) {
        float u0 = clamp(x0), v0 = clamp(y0), u2 = clamp(x2), v2 = clamp(y2);
        float u4 = clamp(x4), v4 = clamp(y4), u6 = clamp(x6), v6 = clamp(y6);
        float u1 = 1 - u0, v1 = 1 - v0, u3 = 1 - u2, v3 = 1 - v2;
        float u5 = 1 - u4, v5 = 1 - v4, u7 = 1 - u6, v7 = 1 - v6;
        switch (i) {
            case 0 -> {
                write(0, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u0, v0, u1, v1, output);
                write(1, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u2, v2, u3, v3, output);
                write(2, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u4, v4, u5, v5, output);
                write(3, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u6, v6, u7, v7, output);
            }
            case 1 -> {
                write(0, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u1, v0, u0, v1, output);
                write(1, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u3, v2, u2, v3, output);
                write(2, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u5, v4, u4, v5, output);
                write(3, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u7, v6, u6, v7, output);
            }
            case 2 -> {
                write(0, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u1, v1, u0, v0, output);
                write(1, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u3, v3, u2, v2, output);
                write(2, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u5, v5, u4, v4, output);
                write(3, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, u7, v7, u6, v6, output);
            }
        }
    }

    private static void partialFace(
        int i,
        AoFaceData faceData,
        float x0,
        float y0,
        float x1,
        float y1,
        float x2,
        float y2,
        float x3,
        float y3,
        QuadInstance output
    ) {
        float a0 = faceData.a0, a1 = faceData.a1, a2 = faceData.a2, a3 = faceData.a3;
        int s0 = faceData.s0, s1 = faceData.s1, s2 = faceData.s2, s3 = faceData.s3;
        int b0 = faceData.b0, b1 = faceData.b1, b2 = faceData.b2, b3 = faceData.b3;
        partialFace(i, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, x0, y0, x1, y1, x2, y2, x3, y3, output);
    }

    private static void partialFace(
        int i,
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float x0,
        float y0,
        float x1,
        float y1,
        float x2,
        float y2,
        float x3,
        float y3,
        QuadInstance output
    ) {
        float a0 = a0(f0, f1, w), a1 = a1(f0, f1, w), a2 = a2(f0, f1, w), a3 = a3(f0, f1, w);
        int s0 = s0(f0, f1, w), s1 = s1(f0, f1, w), s2 = s2(f0, f1, w), s3 = s3(f0, f1, w);
        int b0 = b0(f0, f1, w), b1 = b1(f0, f1, w), b2 = b2(f0, f1, w), b3 = b3(f0, f1, w);
        partialFace(i, a0, a1, a2, a3, s0, s1, s2, s3, b0, b1, b2, b3, x0, y0, x1, y1, x2, y2, x3, y3, output);
    }

    public static void partialFace0(
        AoFaceData faceData,
        float u0,
        float v0,
        float u2,
        float v2,
        float u4,
        float v4,
        float u6,
        float v6,
        QuadInstance output
    ) {
        partialFace(0, faceData, u0, v0, u2, v2, u4, v4, u6, v6, output);
    }

    public static void partialFace0(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u0,
        float v0,
        float u2,
        float v2,
        float u4,
        float v4,
        float u6,
        float v6,
        QuadInstance output
    ) {
        partialFace(0, f0, f1, clamp(w), u0, v0, u2, v2, u4, v4, u6, v6, output);
    }

    public static void partialFlipFace0(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u0,
        float v0,
        float u2,
        float v2,
        float u4,
        float v4,
        float u6,
        float v6,
        QuadInstance output
    ) {
        partialFace(0, f0, f1, 1 - clamp(w), u0, v0, u2, v2, u4, v4, u6, v6, output);
    }

    public static void partialFace1(
        AoFaceData faceData,
        float u1,
        float v0,
        float u3,
        float v2,
        float u5,
        float v4,
        float u7,
        float v6,
        QuadInstance output
    ) {
        partialFace(1, faceData, u1, v0, u3, v2, u5, v4, u7, v6, output);
    }

    public static void partialFace1(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u1,
        float v0,
        float u3,
        float v2,
        float u5,
        float v4,
        float u7,
        float v6,
        QuadInstance output
    ) {
        partialFace(1, f0, f1, clamp(w), u1, v0, u3, v2, u5, v4, u7, v6, output);
    }

    public static void partialFlipFace1(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u1,
        float v0,
        float u3,
        float v2,
        float u5,
        float v4,
        float u7,
        float v6,
        QuadInstance output
    ) {
        partialFace(1, f0, f1, 1 - clamp(w), u1, v0, u3, v2, u5, v4, u7, v6, output);
    }

    public static void partialFace2(
        AoFaceData faceData,
        float u1,
        float v1,
        float u3,
        float v3,
        float u5,
        float v5,
        float u7,
        float v7,
        QuadInstance output
    ) {
        partialFace(2, faceData, u1, v1, u3, v3, u5, v5, u7, v7, output);
    }

    public static void partialFace2(
        AoFaceData f0,
        AoFaceData f1,
        float w,
        float u1,
        float v1,
        float u3,
        float v3,
        float u5,
        float v5,
        float u7,
        float v7,
        QuadInstance output
    ) {
        partialFace(2, f0, f1, clamp(w), u1, v1, u3, v3, u5, v5, u7, v7, output);
    }

    public static int computeFlag(
        boolean b0,
        boolean b1,
        boolean b2,
        boolean b3,
        boolean b4,
        boolean b5,
        boolean b6,
        boolean b7,
        boolean b8
    ) {
        int cubic0 = (b0 ? 2 : 0) ^ (b1 ? 6 : 0);
        int cubic1 = (b2 ? 2 : 0) ^ (b3 ? 6 : 0);
        int cubic2 = (b4 ? 2 : 0) ^ (b5 ? 6 : 0);
        int cubic3 = (b6 ? 2 : 0) ^ (b7 ? 6 : 0);
        int cubic = 1 << cubic0 | 1 << cubic1 | 1 << cubic2 | 1 << cubic3;
        return (cubic == CUBIC_FLAG ? cubic0 : PARTIAL_FLAG) | (b8 ? LIGHT_FACE_FLAG : 0);
    }

    private static int computeFlag(float nx, float ny, float nz) {
        int x = Math.abs(nx) < 1.0E-5F ? 0 : nx > 0 ? 1 : 2;
        int y = Math.abs(ny) < 1.0E-5F ? 0 : ny > 0 ? 3 : 6;
        int z = Math.abs(nz) < 1.0E-5F ? 0 : nz > 0 ? 9 : 18;
        return x + y + z;
    }

    public static void irregularFace(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        Vector3fc normal
    ) {
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();
        switch (computeFlag(x, y, z)) {
            case 1 -> East.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 2 -> West.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 3 -> Up.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 4 -> Two.irregularEastUp(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 5 -> Two.irregularWestUp(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 6 -> Down.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 7 -> Two.irregularEastDown(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 8 -> Two.irregularWestDown(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 9 -> South.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 10 -> Two.irregularEastSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 11 -> Two.irregularWestSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 12 -> Two.irregularUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 13 -> Three.irregularEastUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 14 -> Three.irregularWestUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 15 -> Two.irregularDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 16 -> Three.irregularEastDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 17 -> Three.irregularWestDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 18 -> North.irregularFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 19 -> Two.irregularEastNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 20 -> Two.irregularWestNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 21 -> Two.irregularUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 22 -> Three.irregularEastUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 23 -> Three.irregularWestUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 24 -> Two.irregularDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 25 -> Three.irregularEastDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 26 -> Three.irregularWestDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            default -> {
                output.setColor(0);
                output.setLightCoords(0);
            }
        }
    }

    public static void irregularShadeFace(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        Vector3fc normal
    ) {
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();
        switch (computeFlag(x, y, z)) {
            case 1 -> East.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 2 -> West.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 3 -> Up.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 4 -> Two.irregularShadeEastUp(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 5 -> Two.irregularShadeWestUp(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 6 -> Down.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 7 -> Two.irregularShadeEastDown(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 8 -> Two.irregularShadeWestDown(level, state, pos, output, cache, p0, p1, p2, p3, x, y);
            case 9 -> South.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 10 -> Two.irregularShadeEastSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 11 -> Two.irregularShadeWestSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 12 -> Two.irregularShadeUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 13 -> Three.irregularShadeEastUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 14 -> Three.irregularShadeWestUpSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 15 -> Two.irregularShadeDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 16 -> Three.irregularShadeEastDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 17 -> Three.irregularShadeWestDownSouth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 18 -> North.irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3);
            case 19 -> Two.irregularShadeEastNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 20 -> Two.irregularShadeWestNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, z);
            case 21 -> Two.irregularShadeUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 22 -> Three.irregularShadeEastUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 23 -> Three.irregularShadeWestUpNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 24 -> Two.irregularShadeDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, y, z);
            case 25 -> Three.irregularShadeEastDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            case 26 -> Three.irregularShadeWestDownNorth(level, state, pos, output, cache, p0, p1, p2, p3, x, y, z);
            default -> {
                output.setColor(0);
                output.setLightCoords(0);
            }
        }
    }
}
