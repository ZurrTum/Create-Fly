package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class South extends Face {
    private static int computeFlag(
        float x0,
        float y0,
        float x1,
        float y1,
        float x2,
        float y2,
        float x3,
        float y3,
        float z
    ) {
        boolean b0 = x0 >= EPS_MAX, b1 = y0 <= EPS_MIN;
        boolean b2 = x1 >= EPS_MAX, b3 = y1 <= EPS_MIN;
        boolean b4 = x2 >= EPS_MAX, b5 = y2 <= EPS_MIN;
        boolean b6 = x3 >= EPS_MAX, b7 = y3 <= EPS_MIN;
        boolean b8 = z >= EPS_MAX;
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
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        if (isIrregular(z0, z1, z2, z3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, z0, z1, z2, z3);
            }
            return;
        }
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        switch (computeFlag(x0, y0, x1, y1, x2, y2, x3, y3, z0)) {
            case CUBIC_FLAG_0 -> fullFlipFace0(
                cache.computeSouthBlock(level, state, pos),
                cache.computeSouth(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_0 -> fullFace0(cache.computeSouthBlock(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFlipFace1(
                cache.computeSouthBlock(level, state, pos),
                cache.computeSouth(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_1 -> fullFace1(cache.computeSouthBlock(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFlipFace2(
                cache.computeSouthBlock(level, state, pos),
                cache.computeSouth(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_2 -> fullFace2(cache.computeSouthBlock(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFlipFace3(
                cache.computeSouthBlock(level, state, pos),
                cache.computeSouth(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_3 -> fullFace3(cache.computeSouthBlock(level, state, pos), output);
            case PARTIAL_FLAG -> partialFlipFace0(
                cache.computeSouthBlock(level, state, pos),
                cache.computeSouth(level, state, pos),
                z0,
                x0,
                y0,
                x1,
                y1,
                x2,
                y2,
                x3,
                y3,
                output
            );
            case LIGHT_FACE_FLAG | PARTIAL_FLAG ->
                partialFace0(cache.computeSouthBlock(level, state, pos), x0, y0, x1, y1, x2, y2, x3, y3, output);
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
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        if (isIrregular(z0, z1, z2, z3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, z0, z1, z2, z3);
            }
            return;
        }
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        switch (computeFlag(x0, y0, x1, y1, x2, y2, x3, y3, z0)) {
            case CUBIC_FLAG_0 -> fullFlipFace0(
                cache.computeSouthBlockShade(level, state, pos),
                cache.computeSouthShade(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_0 -> fullFace0(cache.computeSouthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFlipFace1(
                cache.computeSouthBlockShade(level, state, pos),
                cache.computeSouthShade(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_1 -> fullFace1(cache.computeSouthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFlipFace2(
                cache.computeSouthBlockShade(level, state, pos),
                cache.computeSouthShade(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_2 -> fullFace2(cache.computeSouthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFlipFace3(
                cache.computeSouthBlockShade(level, state, pos),
                cache.computeSouthShade(level, state, pos),
                z0,
                output
            );
            case LIGHT_FACE_FLAG | CUBIC_FLAG_3 -> fullFace3(cache.computeSouthBlockShade(level, state, pos), output);
            case PARTIAL_FLAG -> partialFlipFace0(
                cache.computeSouthBlockShade(level, state, pos),
                cache.computeSouthShade(level, state, pos),
                z0,
                x0,
                y0,
                x1,
                y1,
                x2,
                y2,
                x3,
                y3,
                output
            );
            case LIGHT_FACE_FLAG | PARTIAL_FLAG ->
                partialFace0(cache.computeSouthBlockShade(level, state, pos), x0, y0, x1, y1, x2, y2, x3, y3, output);
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
        irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.z(), p1.z(), p2.z(), p3.z());
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
        float z0,
        float z1,
        float z2,
        float z3
    ) {
        irregularFace(0, level, state, pos, output, cache, z0, p0.x(), p0.y());
        irregularFace(1, level, state, pos, output, cache, z1, p1.x(), p1.y());
        irregularFace(2, level, state, pos, output, cache, z2, p2.x(), p2.y());
        irregularFace(3, level, state, pos, output, cache, z3, p3.x(), p3.y());
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
        irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.z(), p1.z(), p2.z(), p3.z());
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
        float z0,
        float z1,
        float z2,
        float z3
    ) {
        irregularShadeFace(0, level, state, pos, output, cache, z0, p0.x(), p0.y());
        irregularShadeFace(1, level, state, pos, output, cache, z1, p1.x(), p1.y());
        irregularShadeFace(2, level, state, pos, output, cache, z2, p2.x(), p2.y());
        irregularShadeFace(3, level, state, pos, output, cache, z3, p3.x(), p3.y());
    }

    private static void irregularFace(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        float z,
        float x,
        float y
    ) {
        float w = 1 - clamp(z), u0 = clamp(x), v0 = clamp(y);
        float u1 = 1 - u0, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeSouthBlock(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeSouth(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeSouthBlock(level, state, pos);
            AoFaceData f1 = cache.computeSouth(level, state, pos);
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
        float z,
        float x,
        float y
    ) {
        float w = 1 - clamp(z), u0 = clamp(x), v0 = clamp(y);
        float u1 = 1 - u0, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeSouthBlockShade(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeSouthShade(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeSouthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeSouthShade(level, state, pos);
            write(i, f0, f1, w, u0, v0, u1, v1, output);
        }
    }
}
