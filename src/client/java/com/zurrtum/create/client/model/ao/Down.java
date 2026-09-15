package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class Down extends Face {
    private static int computeFlag(
        float x0,
        float z0,
        float x1,
        float z1,
        float x2,
        float z2,
        float x3,
        float z3,
        float y
    ) {
        boolean b0 = x0 >= EPS_MAX, b1 = z0 <= EPS_MIN;
        boolean b2 = x1 >= EPS_MAX, b3 = z1 <= EPS_MIN;
        boolean b4 = x2 >= EPS_MAX, b5 = z2 <= EPS_MIN;
        boolean b6 = x3 >= EPS_MAX, b7 = z3 <= EPS_MIN;
        boolean b8 = y <= EPS_MIN;
        return computeFlag(b0, b1, b2, b3, b4, b5, b6, b7, b8);
    }

    public static void prepareQuad(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Object normals,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        QuadInstance output,
        AoFaceDataCache cache
    ) {
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        if (isIrregular(y0, y1, y2, y3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, y0, y1, y2, y3);
            }
            return;
        }
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        switch (computeFlag(x0, z0, x1, z1, x2, z2, x3, z3, y0)) {
            case CUBIC_FLAG_0 ->
                fullFace0(cache.computeDownBlock(level, state, pos), cache.computeDown(level, state, pos), y0, output);
            case LIGHT_FACE_FLAG | CUBIC_FLAG_0 -> fullFace0(cache.computeDownBlock(level, state, pos), output);
            case CUBIC_FLAG_1 ->
                fullFace1(cache.computeDownBlock(level, state, pos), cache.computeDown(level, state, pos), y0, output);
            case LIGHT_FACE_FLAG | CUBIC_FLAG_1 -> fullFace1(cache.computeDownBlock(level, state, pos), output);
            case CUBIC_FLAG_2 ->
                fullFace2(cache.computeDownBlock(level, state, pos), cache.computeDown(level, state, pos), y0, output);
            case LIGHT_FACE_FLAG | CUBIC_FLAG_2 -> fullFace2(cache.computeDownBlock(level, state, pos), output);
            case CUBIC_FLAG_3 ->
                fullFace3(cache.computeDownBlock(level, state, pos), cache.computeDown(level, state, pos), y0, output);
            case LIGHT_FACE_FLAG | CUBIC_FLAG_3 -> fullFace3(cache.computeDownBlock(level, state, pos), output);
            case PARTIAL_FLAG -> partialFace0(
                cache.computeDownBlock(level, state, pos),
                cache.computeDown(level, state, pos),
                y0,
                x0,
                z0,
                x1,
                z1,
                x2,
                z2,
                x3,
                z3,
                output
            );
            case LIGHT_FACE_FLAG | PARTIAL_FLAG ->
                partialFace0(cache.computeDownBlock(level, state, pos), x0, z0, x1, z1, x2, z2, x3, z3, output);
        }
    }

    public static void prepareShadeQuad(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        Object normals,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        QuadInstance output,
        AoFaceDataCache cache
    ) {
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        if (isIrregular(y0, y1, y2, y3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, y0, y1, y2, y3);
            }
            return;
        }
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        switch (computeFlag(x0, z0, x1, z1, x2, z2, x3, z3, y0)) {
            case CUBIC_FLAG_0 -> fullFace0(
                cache.computeDownBlockShade(level, state, pos),
                cache.computeDownShade(level, state, pos),
                y0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_0 -> fullFace0(cache.computeDownBlockShade(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFace1(
                cache.computeDownBlockShade(level, state, pos),
                cache.computeDownShade(level, state, pos),
                y0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_1 -> fullFace1(cache.computeDownBlockShade(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFace2(
                cache.computeDownBlockShade(level, state, pos),
                cache.computeDownShade(level, state, pos),
                y0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_2 -> fullFace2(cache.computeDownBlockShade(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFace3(
                cache.computeDownBlockShade(level, state, pos),
                cache.computeDownShade(level, state, pos),
                y0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_3 -> fullFace3(cache.computeDownBlockShade(level, state, pos), output);
            case PARTIAL_FLAG -> partialFace0(
                cache.computeDownBlockShade(level, state, pos),
                cache.computeDownShade(level, state, pos),
                y0,
                x0,
                z0,
                x1,
                z1,
                x2,
                z2,
                x3,
                z3,
                output
            );
            case LIGHT_FACE_FLAG | PARTIAL_FLAG ->
                partialFace0(cache.computeDownBlockShade(level, state, pos), x0, z0, x1, z1, x2, z2, x3, z3, output);
        }
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
        Vector3fc p3
    ) {
        irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.y(), p1.y(), p2.y(), p3.y());
    }

    private static void irregularFace(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y0,
        float y1,
        float y2,
        float y3
    ) {
        irregularFace(0, level, state, pos, output, cache, y0, p0.x(), p0.z());
        irregularFace(1, level, state, pos, output, cache, y1, p1.x(), p1.z());
        irregularFace(2, level, state, pos, output, cache, y2, p2.x(), p2.z());
        irregularFace(3, level, state, pos, output, cache, y3, p3.x(), p3.z());
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
        Vector3fc p3
    ) {
        irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.y(), p1.y(), p2.y(), p3.y());
    }

    private static void irregularShadeFace(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        Vector3fc p0,
        Vector3fc p1,
        Vector3fc p2,
        Vector3fc p3,
        float y0,
        float y1,
        float y2,
        float y3
    ) {
        irregularShadeFace(0, level, state, pos, output, cache, y0, p0.x(), p0.z());
        irregularShadeFace(1, level, state, pos, output, cache, y1, p1.x(), p1.z());
        irregularShadeFace(2, level, state, pos, output, cache, y2, p2.x(), p2.z());
        irregularShadeFace(3, level, state, pos, output, cache, y3, p3.x(), p3.z());
    }

    private static void irregularFace(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        float y,
        float x,
        float z
    ) {
        float w = clamp(y), u0 = clamp(x), v0 = clamp(z);
        float u1 = 1 - u0, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeDownBlock(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeDown(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeDownBlock(level, state, pos);
            AoFaceData f1 = cache.computeDown(level, state, pos);
            write(i, f0, f1, w, u0, v0, u1, v1, output);
        }
    }

    private static void irregularShadeFace(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        float y,
        float x,
        float z
    ) {
        float w = clamp(y), u0 = clamp(x), v0 = clamp(z);
        float u1 = 1 - u0, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeDownBlockShade(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeDownShade(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeDownBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeDownShade(level, state, pos);
            write(i, f0, f1, w, u0, v0, u1, v1, output);
        }
    }
}
