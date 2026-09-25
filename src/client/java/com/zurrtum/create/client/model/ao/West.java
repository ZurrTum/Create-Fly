package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class West extends Face {
    private static int computeFlag(
        float y0,
        float z0,
        float y1,
        float z1,
        float y2,
        float z2,
        float y3,
        float z3,
        float x
    ) {
        if (y0 <= EPS_MIN) {
            if (z0 <= EPS_MIN) {
                if (y1 <= EPS_MIN) {
                    if (z1 >= EPS_MAX) {
                        if (y2 >= EPS_MAX) {
                            if (z2 <= EPS_MIN) {
                                if (y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                    return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                                }
                            } else if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        }
                    }
                } else if (y1 >= EPS_MAX) {
                    if (z1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        } else if (y2 >= EPS_MAX & z2 >= EPS_MAX & y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                        }
                    } else if (z1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        } else if (y2 >= EPS_MAX & z2 <= EPS_MIN & y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                        }
                    }
                }
            } else if (z0 >= EPS_MAX) {
                if (y1 <= EPS_MIN) {
                    if (z1 <= EPS_MIN) {
                        if (y2 >= EPS_MAX) {
                            if (z2 <= EPS_MIN) {
                                if (y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                    return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                                }
                            } else if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        }
                    }
                } else if (y1 >= EPS_MAX) {
                    if (z1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (z2 <= EPS_MIN & y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        } else if (y2 >= EPS_MAX & z2 >= EPS_MAX & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                        }
                    } else if (z1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (z2 <= EPS_MIN & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        } else if (y2 >= EPS_MAX & z2 <= EPS_MIN & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                        }
                    }
                }
            }
        } else if (y0 >= EPS_MAX) {
            if (z0 <= EPS_MIN) {
                if (y1 <= EPS_MIN) {
                    if (z1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                            }
                        } else if (y2 >= EPS_MAX & z2 >= EPS_MAX & y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    } else if (z1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (z2 <= EPS_MIN & y3 >= EPS_MAX & z3 >= EPS_MAX) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                            }
                        } else if (y2 >= EPS_MAX & z2 >= EPS_MAX & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    }
                } else if (y1 >= EPS_MAX & z1 >= EPS_MAX & y2 <= EPS_MIN) {
                    if (z2 <= EPS_MIN) {
                        if (y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    } else if (z2 >= EPS_MAX & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                        return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                    }
                }
            } else if (z0 >= EPS_MAX) {
                if (y1 <= EPS_MIN) {
                    if (z1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (z2 >= EPS_MAX & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                            }
                        } else if (y2 >= EPS_MAX & z2 <= EPS_MIN & y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    } else if (z1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (z2 <= EPS_MIN & y3 >= EPS_MAX & z3 <= EPS_MIN) {
                                return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                            }
                        } else if (y2 >= EPS_MAX & z2 <= EPS_MIN & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    }
                } else if (y1 >= EPS_MAX & z1 <= EPS_MIN & y2 <= EPS_MIN) {
                    if (z2 <= EPS_MIN) {
                        if (y3 <= EPS_MIN & z3 >= EPS_MAX) {
                            return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    } else if (z2 >= EPS_MAX & y3 <= EPS_MIN & z3 <= EPS_MIN) {
                        return x <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                    }
                }
            }
        }
        return x <= EPS_MIN ? PARTIAL_LIGHT_FACE_FLAG : PARTIAL_FLAG;
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
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        if (isIrregular(x0, x1, x2, x3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, x0, x1, x2, x3);
            }
            return;
        }
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        switch (computeFlag(y0, z0, y1, z1, y2, z2, y3, z3, x0)) {
            case PARTIAL_FLAG -> partialFace1(
                cache.computeWestBlock(level, state, pos),
                cache.computeWest(level, state, pos),
                x0,
                y0,
                z0,
                y1,
                z1,
                y2,
                z2,
                y3,
                z3,
                output
            );
            case PARTIAL_LIGHT_FACE_FLAG ->
                partialFace1(cache.computeWestBlock(level, state, pos), y0, z0, y1, z1, y2, z2, y3, z3, output);
            case CUBIC_FLAG_0 ->
                fullFace0(cache.computeWestBlock(level, state, pos), cache.computeWest(level, state, pos), x0, output);
            case CUBIC_LIGHT_FACE_FLAG_0 -> fullFace0(cache.computeWestBlock(level, state, pos), output);
            case CUBIC_FLAG_1 ->
                fullFace1(cache.computeWestBlock(level, state, pos), cache.computeWest(level, state, pos), x0, output);
            case CUBIC_LIGHT_FACE_FLAG_1 -> fullFace1(cache.computeWestBlock(level, state, pos), output);
            case CUBIC_FLAG_2 ->
                fullFace2(cache.computeWestBlock(level, state, pos), cache.computeWest(level, state, pos), x0, output);
            case CUBIC_LIGHT_FACE_FLAG_2 -> fullFace2(cache.computeWestBlock(level, state, pos), output);
            case CUBIC_FLAG_3 ->
                fullFace3(cache.computeWestBlock(level, state, pos), cache.computeWest(level, state, pos), x0, output);
            case CUBIC_LIGHT_FACE_FLAG_3 -> fullFace3(cache.computeWestBlock(level, state, pos), output);
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
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        if (isIrregular(x0, x1, x2, x3)) {
            Vector3fc normal = ((NormalsBakedQuad) normals).create$getNormal();
            if (normal != null) {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, normal);
            } else {
                irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, x0, x1, x2, x3);
            }
            return;
        }
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        float z0 = p0.z(), z1 = p1.z(), z2 = p2.z(), z3 = p3.z();
        switch (computeFlag(y0, z0, y1, z1, y2, z2, y3, z3, x0)) {
            case PARTIAL_FLAG -> partialFace1(
                cache.computeWestBlockShade(level, state, pos),
                cache.computeWestShade(level, state, pos),
                x0,
                y0,
                z0,
                y1,
                z1,
                y2,
                z2,
                y3,
                z3,
                output
            );
            case PARTIAL_LIGHT_FACE_FLAG ->
                partialFace1(cache.computeWestBlockShade(level, state, pos), y0, z0, y1, z1, y2, z2, y3, z3, output);
            case CUBIC_FLAG_0 -> fullFace0(
                cache.computeWestBlockShade(level, state, pos),
                cache.computeWestShade(level, state, pos),
                x0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_0 -> fullFace0(cache.computeWestBlockShade(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFace1(
                cache.computeWestBlockShade(level, state, pos),
                cache.computeWestShade(level, state, pos),
                x0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_1 -> fullFace1(cache.computeWestBlockShade(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFace2(
                cache.computeWestBlockShade(level, state, pos),
                cache.computeWestShade(level, state, pos),
                x0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_2 -> fullFace2(cache.computeWestBlockShade(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFace3(
                cache.computeWestBlockShade(level, state, pos),
                cache.computeWestShade(level, state, pos),
                x0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_3 -> fullFace3(cache.computeWestBlockShade(level, state, pos), output);
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
        irregularFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.x(), p1.x(), p2.x(), p3.x());
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
        float x0,
        float x1,
        float x2,
        float x3
    ) {
        irregularFace(0, level, state, pos, output, cache, x0, p0.y(), p0.z());
        irregularFace(1, level, state, pos, output, cache, x1, p1.y(), p1.z());
        irregularFace(2, level, state, pos, output, cache, x2, p2.y(), p2.z());
        irregularFace(3, level, state, pos, output, cache, x3, p3.y(), p3.z());
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
        irregularShadeFace(level, state, pos, output, cache, p0, p1, p2, p3, p0.x(), p1.x(), p2.x(), p3.x());
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
        float x0,
        float x1,
        float x2,
        float x3
    ) {
        irregularShadeFace(0, level, state, pos, output, cache, x0, p0.y(), p0.z());
        irregularShadeFace(1, level, state, pos, output, cache, x1, p1.y(), p1.z());
        irregularShadeFace(2, level, state, pos, output, cache, x2, p2.y(), p2.z());
        irregularShadeFace(3, level, state, pos, output, cache, x3, p3.y(), p3.z());
    }

    private static void irregularFace(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        float x,
        float y,
        float z
    ) {
        float w = clamp(x), u1 = clamp(y), v0 = clamp(z);
        float u0 = 1 - u1, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeWestBlock(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeWest(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeWestBlock(level, state, pos);
            AoFaceData f1 = cache.computeWest(level, state, pos);
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
        float x,
        float y,
        float z
    ) {
        float w = clamp(x), u1 = clamp(y), v0 = clamp(z);
        float u0 = 1 - u1, v1 = 1 - v0;
        if (w < 0.00001f) {
            write(i, cache.computeWestBlockShade(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeWestShade(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeWestBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeWestShade(level, state, pos);
            write(i, f0, f1, w, u0, v0, u1, v1, output);
        }
    }
}
