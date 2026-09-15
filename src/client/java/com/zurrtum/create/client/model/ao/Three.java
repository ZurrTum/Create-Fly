package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class Three extends Face {
    public static void irregularEastUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularEastUpSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularEastUpSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularEastUpSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularEastUpSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeEastUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeEastUpSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeEastUpSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeEastUpSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeEastUpSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularEastUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEast(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlock(level, state, pos);
            AoFaceData f1 = cache.computeEast(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeEastUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularWestUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularWestUpSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularWestUpSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularWestUpSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularWestUpSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeWestUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeWestUpSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeWestUpSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeWestUpSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeWestUpSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularWestUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWest(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlock(level, state, pos);
            AoFaceData f1 = cache.computeWest(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeWestUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularEastDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularEastDownSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularEastDownSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularEastDownSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularEastDownSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeEastDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeEastDownSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeEastDownSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeEastDownSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeEastDownSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularEastDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEast(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlock(level, state, pos);
            AoFaceData f1 = cache.computeEast(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeEastDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularWestDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularWestDownSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularWestDownSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularWestDownSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularWestDownSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeWestDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeWestDownSouth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeWestDownSouth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeWestDownSouth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeWestDownSouth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularWestDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWest(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlock(level, state, pos);
            AoFaceData f1 = cache.computeWest(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeWestDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = fx * cy, w9 = fx * fy, w10 = cx * fy, w11 = cx * cy;
        int b2, s2;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a2 = weightedAo(f0, f1, fz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, fz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, fz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularEastUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularEastUpNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularEastUpNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularEastUpNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularEastUpNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeEastUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeEastUpNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeEastUpNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeEastUpNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeEastUpNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularEastUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEast(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlock(level, state, pos);
            AoFaceData f1 = cache.computeEast(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeEastUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularWestUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularWestUpNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularWestUpNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularWestUpNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularWestUpNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeWestUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeWestUpNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeWestUpNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeWestUpNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeWestUpNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularWestUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWest(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlock(level, state, pos);
            AoFaceData f1 = cache.computeWest(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeWestUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = cx * cz, w5 = cx * fz, w6 = fx * fz, w7 = fx * cz;
        int b1, s1;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a1 = weightedAo(f0, f1, fy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularEastDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularEastDownNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularEastDownNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularEastDownNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularEastDownNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeEastDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeEastDownNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeEastDownNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeEastDownNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeEastDownNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularEastDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEast(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlock(level, state, pos);
            AoFaceData f1 = cache.computeEast(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeEastDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fy * cz, w1 = fy * fz, w2 = cy * fz, w3 = cy * cz;
        int b0, s0;
        if (fx < 0.00001f) {
            AoFaceData faceData = cache.computeEastBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fx > 0.99999f) {
            AoFaceData faceData = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeEastBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeEastShade(level, state, pos);
            a0 = weightedAo(f0, f1, fx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    public static void irregularWestDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularWestDownNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularWestDownNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularWestDownNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularWestDownNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    public static void irregularShadeWestDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float x,
        float y,
        float z
    ) {
        float x2 = x * x, y2 = y * y, z2 = z * z;
        irregularShadeWestDownNorth(0, level, state, pos, output, cache, p0, x2, y2, z2);
        irregularShadeWestDownNorth(1, level, state, pos, output, cache, p1, x2, y2, z2);
        irregularShadeWestDownNorth(2, level, state, pos, output, cache, p2, x2, y2, z2);
        irregularShadeWestDownNorth(3, level, state, pos, output, cache, p3, x2, y2, z2);
    }

    private static void irregularWestDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWest(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlock(level, state, pos);
            AoFaceData f1 = cache.computeWest(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }

    private static void irregularShadeWestDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cy * cz, w1 = cy * fz, w2 = fy * fz, w3 = fy * cz;
        int b0, s0;
        if (cx < 0.00001f) {
            AoFaceData faceData = cache.computeWestBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cx > 0.99999f) {
            AoFaceData faceData = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeWestBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeWestShade(level, state, pos);
            a0 = weightedAo(f0, f1, cx, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cx, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cx, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cz, w5 = fx * fz, w6 = cx * fz, w7 = cx * cz;
        int b1, s1;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a1 = weightedAo(f0, f1, cy, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cy, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cy, w4, w5, w6, w7);
        }
        float a2, w8 = cy * fx, w9 = cy * cx, w10 = fy * cx, w11 = fy * fx;
        int b2, s2;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(faceData, w8, w9, w10, w11);
            b2 = weightedBlockLight(faceData, w8, w9, w10, w11);
            s2 = weightedSkyLight(faceData, w8, w9, w10, w11);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a2 = weightedAo(f0, f1, cz, w8, w9, w10, w11);
            b2 = weightedBlockLight(f0, f1, cz, w8, w9, w10, w11);
            s2 = weightedSkyLight(f0, f1, cz, w8, w9, w10, w11);
        }
        write(i, a0, a1, a2, s0, s1, s2, b0, b1, b2, x2, y2, z2, output);
    }
}
