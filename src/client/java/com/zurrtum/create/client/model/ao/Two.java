package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class Two extends Face {
    public static void irregularEastUp(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularEastUp(0, level, state, pos, output, cache, p0, x2, y2);
        irregularEastUp(1, level, state, pos, output, cache, p1, x2, y2);
        irregularEastUp(2, level, state, pos, output, cache, p2, x2, y2);
        irregularEastUp(3, level, state, pos, output, cache, p3, x2, y2);
    }

    public static void irregularShadeEastUp(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularShadeEastUp(0, level, state, pos, output, cache, p0, x2, y2);
        irregularShadeEastUp(1, level, state, pos, output, cache, p1, x2, y2);
        irregularShadeEastUp(2, level, state, pos, output, cache, p2, x2, y2);
        irregularShadeEastUp(3, level, state, pos, output, cache, p3, x2, y2);
    }

    private static void irregularEastUp(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    private static void irregularShadeEastUp(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    public static void irregularWestUp(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularWestUp(0, level, state, pos, output, cache, p0, x2, y2);
        irregularWestUp(1, level, state, pos, output, cache, p1, x2, y2);
        irregularWestUp(2, level, state, pos, output, cache, p2, x2, y2);
        irregularWestUp(3, level, state, pos, output, cache, p3, x2, y2);
    }

    public static void irregularShadeWestUp(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularShadeWestUp(0, level, state, pos, output, cache, p0, x2, y2);
        irregularShadeWestUp(1, level, state, pos, output, cache, p1, x2, y2);
        irregularShadeWestUp(2, level, state, pos, output, cache, p2, x2, y2);
        irregularShadeWestUp(3, level, state, pos, output, cache, p3, x2, y2);
    }

    private static void irregularWestUp(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    private static void irregularShadeWestUp(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    public static void irregularEastDown(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularEastDown(0, level, state, pos, output, cache, p0, x2, y2);
        irregularEastDown(1, level, state, pos, output, cache, p1, x2, y2);
        irregularEastDown(2, level, state, pos, output, cache, p2, x2, y2);
        irregularEastDown(3, level, state, pos, output, cache, p3, x2, y2);
    }

    public static void irregularShadeEastDown(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularShadeEastDown(0, level, state, pos, output, cache, p0, x2, y2);
        irregularShadeEastDown(1, level, state, pos, output, cache, p1, x2, y2);
        irregularShadeEastDown(2, level, state, pos, output, cache, p2, x2, y2);
        irregularShadeEastDown(3, level, state, pos, output, cache, p3, x2, y2);
    }

    private static void irregularEastDown(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    private static void irregularShadeEastDown(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    public static void irregularWestDown(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularWestDown(0, level, state, pos, output, cache, p0, x2, y2);
        irregularWestDown(1, level, state, pos, output, cache, p1, x2, y2);
        irregularWestDown(2, level, state, pos, output, cache, p2, x2, y2);
        irregularWestDown(3, level, state, pos, output, cache, p3, x2, y2);
    }

    public static void irregularShadeWestDown(
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
        float y
    ) {
        float x2 = x * x, y2 = y * y;
        irregularShadeWestDown(0, level, state, pos, output, cache, p0, x2, y2);
        irregularShadeWestDown(1, level, state, pos, output, cache, p1, x2, y2);
        irregularShadeWestDown(2, level, state, pos, output, cache, p2, x2, y2);
        irregularShadeWestDown(3, level, state, pos, output, cache, p3, x2, y2);
    }

    private static void irregularWestDown(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    private static void irregularShadeWestDown(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
        float y2
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
        write(i, a0, a1, s0, s1, b0, b1, x2, y2, output);
    }

    public static void irregularEastSouth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularEastSouth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularEastSouth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularEastSouth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularEastSouth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    public static void irregularShadeEastSouth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularShadeEastSouth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularShadeEastSouth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularShadeEastSouth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularShadeEastSouth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    private static void irregularEastSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    private static void irregularShadeEastSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    public static void irregularWestSouth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularWestSouth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularWestSouth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularWestSouth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularWestSouth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    public static void irregularShadeWestSouth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularShadeWestSouth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularShadeWestSouth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularShadeWestSouth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularShadeWestSouth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    private static void irregularWestSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    private static void irregularShadeWestSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    public static void irregularUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularUpSouth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularUpSouth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularUpSouth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularUpSouth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    public static void irregularShadeUpSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularShadeUpSouth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularShadeUpSouth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularShadeUpSouth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularShadeUpSouth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    private static void irregularUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cx * cz, w1 = cx * fz, w2 = fx * fz, w3 = fx * cz;
        int b0, s0;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a0 = weightedAo(f0, f1, fy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fy, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    private static void irregularShadeUpSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cx * cz, w1 = cx * fz, w2 = fx * fz, w3 = fx * cz;
        int b0, s0;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a0 = weightedAo(f0, f1, fy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fy, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    public static void irregularDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularDownSouth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularDownSouth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularDownSouth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularDownSouth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    public static void irregularShadeDownSouth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularShadeDownSouth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularShadeDownSouth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularShadeDownSouth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularShadeDownSouth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    private static void irregularDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fx * cz, w1 = fx * fz, w2 = cx * fz, w3 = cx * cz;
        int b0, s0;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a0 = weightedAo(f0, f1, cy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cy, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    private static void irregularShadeDownSouth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fx * cz, w1 = fx * fz, w2 = cx * fz, w3 = cx * cz;
        int b0, s0;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a0 = weightedAo(f0, f1, cy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cy, w0, w1, w2, w3);
        }
        float a1, w4 = fx * cy, w5 = fx * fy, w6 = cx * fy, w7 = cx * cy;
        int b1, s1;
        if (fz < 0.00001f) {
            AoFaceData faceData = cache.computeSouthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (fz > 0.99999f) {
            AoFaceData faceData = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            a1 = weightedAo(f0, f1, fz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, fz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, fz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    public static void irregularEastNorth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularEastNorth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularEastNorth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularEastNorth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularEastNorth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    public static void irregularShadeEastNorth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularShadeEastNorth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularShadeEastNorth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularShadeEastNorth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularShadeEastNorth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    private static void irregularEastNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    private static void irregularShadeEastNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    public static void irregularWestNorth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularWestNorth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularWestNorth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularWestNorth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularWestNorth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    public static void irregularShadeWestNorth(
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
        float z
    ) {
        float x2 = x * x, z2 = z * z;
        irregularShadeWestNorth(0, level, state, pos, output, cache, p0, x2, z2);
        irregularShadeWestNorth(1, level, state, pos, output, cache, p1, x2, z2);
        irregularShadeWestNorth(2, level, state, pos, output, cache, p2, x2, z2);
        irregularShadeWestNorth(3, level, state, pos, output, cache, p3, x2, z2);
    }

    private static void irregularWestNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    private static void irregularShadeWestNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float x2,
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
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, x2, z2, output);
    }

    public static void irregularUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularUpNorth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularUpNorth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularUpNorth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularUpNorth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    public static void irregularShadeUpNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularShadeUpNorth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularShadeUpNorth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularShadeUpNorth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularShadeUpNorth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    private static void irregularUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cx * cz, w1 = cx * fz, w2 = fx * fz, w3 = fx * cz;
        int b0, s0;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUp(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeUpBlock(level, state, pos);
            AoFaceData f1 = cache.computeUp(level, state, pos);
            a0 = weightedAo(f0, f1, fy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fy, w0, w1, w2, w3);
        }
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    private static void irregularShadeUpNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = cx * cz, w1 = cx * fz, w2 = fx * fz, w3 = fx * cz;
        int b0, s0;
        if (fy < 0.00001f) {
            AoFaceData faceData = cache.computeUpBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (fy > 0.99999f) {
            AoFaceData faceData = cache.computeUpShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeUpBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeUpShade(level, state, pos);
            a0 = weightedAo(f0, f1, fy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, fy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, fy, w0, w1, w2, w3);
        }
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    public static void irregularDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularDownNorth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularDownNorth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularDownNorth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularDownNorth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    public static void irregularShadeDownNorth(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y,
        float z
    ) {
        float y2 = y * y, z2 = z * z;
        irregularShadeDownNorth(0, level, state, pos, output, cache, p0, y2, z2);
        irregularShadeDownNorth(1, level, state, pos, output, cache, p1, y2, z2);
        irregularShadeDownNorth(2, level, state, pos, output, cache, p2, y2, z2);
        irregularShadeDownNorth(3, level, state, pos, output, cache, p3, y2, z2);
    }

    private static void irregularDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fx * cz, w1 = fx * fz, w2 = cx * fz, w3 = cx * cz;
        int b0, s0;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlock(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDown(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            a0 = weightedAo(f0, f1, cy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cy, w0, w1, w2, w3);
        }
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlock(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorth(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }

    private static void irregularShadeDownNorth(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc position,
        float y2,
        float z2
    ) {
        float cx = clamp(position.x()), cy = clamp(position.y()), cz = clamp(position.z());
        float fx = 1 - cx, fy = 1 - cy, fz = 1 - cz;
        float a0, w0 = fx * cz, w1 = fx * fz, w2 = cx * fz, w3 = cx * cz;
        int b0, s0;
        if (cy < 0.00001f) {
            AoFaceData faceData = cache.computeDownBlockShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else if (cy > 0.99999f) {
            AoFaceData faceData = cache.computeDownShade(level, state, pos);
            a0 = weightedAo(faceData, w0, w1, w2, w3);
            b0 = weightedBlockLight(faceData, w0, w1, w2, w3);
            s0 = weightedSkyLight(faceData, w0, w1, w2, w3);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            a0 = weightedAo(f0, f1, cy, w0, w1, w2, w3);
            b0 = weightedBlockLight(f0, f1, cy, w0, w1, w2, w3);
            s0 = weightedSkyLight(f0, f1, cy, w0, w1, w2, w3);
        }
        float a1, w4 = cy * fx, w5 = cy * cx, w6 = fy * cx, w7 = fy * fx;
        int b1, s1;
        if (cz < 0.00001f) {
            AoFaceData faceData = cache.computeNorthBlockShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else if (cz > 0.99999f) {
            AoFaceData faceData = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(faceData, w4, w5, w6, w7);
            b1 = weightedBlockLight(faceData, w4, w5, w6, w7);
            s1 = weightedSkyLight(faceData, w4, w5, w6, w7);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            a1 = weightedAo(f0, f1, cz, w4, w5, w6, w7);
            b1 = weightedBlockLight(f0, f1, cz, w4, w5, w6, w7);
            s1 = weightedSkyLight(f0, f1, cz, w4, w5, w6, w7);
        }
        write(i, a0, a1, s0, s1, b0, b1, y2, z2, output);
    }
}
