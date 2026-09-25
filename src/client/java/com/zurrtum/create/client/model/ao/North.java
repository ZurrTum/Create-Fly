package com.zurrtum.create.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class North extends Face {
    private static int computeFlag(
        float y0,
        float x0,
        float y1,
        float x1,
        float y2,
        float x2,
        float y3,
        float x3,
        float z
    ) {
        if (y0 <= EPS_MIN) {
            if (x0 >= EPS_MAX) {
                if (y1 <= EPS_MIN) {
                    if (x1 <= EPS_MIN) {
                        if (y2 >= EPS_MAX) {
                            if (x2 >= EPS_MAX) {
                                if (y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                    return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                                }
                            } else if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        }
                    }
                } else if (y1 >= EPS_MAX) {
                    if (x1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        } else if (y2 >= EPS_MAX & x2 <= EPS_MIN & y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                        }
                    } else if (x1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                            }
                        } else if (y2 >= EPS_MAX & x2 >= EPS_MAX & y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_2 : CUBIC_FLAG_2;
                        }
                    }
                }
            } else if (x0 <= EPS_MIN) {
                if (y1 <= EPS_MIN) {
                    if (x1 >= EPS_MAX) {
                        if (y2 >= EPS_MAX) {
                            if (x2 >= EPS_MAX) {
                                if (y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                    return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                                }
                            } else if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        }
                    }
                } else if (y1 >= EPS_MAX) {
                    if (x1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (x2 >= EPS_MAX & y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        } else if (y2 >= EPS_MAX & x2 <= EPS_MIN & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                        }
                    } else if (x1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (x2 >= EPS_MAX & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                            }
                        } else if (y2 >= EPS_MAX & x2 >= EPS_MAX & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_1 : CUBIC_FLAG_1;
                        }
                    }
                }
            }
        } else if (y0 >= EPS_MAX) {
            if (x0 >= EPS_MAX) {
                if (y1 <= EPS_MIN) {
                    if (x1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                            }
                        } else if (y2 >= EPS_MAX & x2 <= EPS_MIN & y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    } else if (x1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (x2 >= EPS_MAX & y3 >= EPS_MAX & x3 <= EPS_MIN) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                            }
                        } else if (y2 >= EPS_MAX & x2 <= EPS_MIN & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    }
                } else if (y1 >= EPS_MAX & x1 <= EPS_MIN & y2 <= EPS_MIN) {
                    if (x2 >= EPS_MAX) {
                        if (y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                        }
                    } else if (x2 <= EPS_MIN & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                        return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_3 : CUBIC_FLAG_3;
                    }
                }
            } else if (x0 <= EPS_MIN) {
                if (y1 <= EPS_MIN) {
                    if (x1 >= EPS_MAX) {
                        if (y2 <= EPS_MIN) {
                            if (x2 <= EPS_MIN & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                            }
                        } else if (y2 >= EPS_MAX & x2 >= EPS_MAX & y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    } else if (x1 <= EPS_MIN) {
                        if (y2 <= EPS_MIN) {
                            if (x2 >= EPS_MAX & y3 >= EPS_MAX & x3 >= EPS_MAX) {
                                return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                            }
                        } else if (y2 >= EPS_MAX & x2 >= EPS_MAX & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    }
                } else if (y1 >= EPS_MAX & x1 >= EPS_MAX & y2 <= EPS_MIN) {
                    if (x2 >= EPS_MAX) {
                        if (y3 <= EPS_MIN & x3 <= EPS_MIN) {
                            return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                        }
                    } else if (x2 <= EPS_MIN & y3 <= EPS_MIN & x3 >= EPS_MAX) {
                        return z <= EPS_MIN ? CUBIC_LIGHT_FACE_FLAG_0 : CUBIC_FLAG_0;
                    }
                }
            }
        }
        return z <= EPS_MIN ? PARTIAL_LIGHT_FACE_FLAG : PARTIAL_FLAG;
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
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        switch (computeFlag(y0, x0, y1, x1, y2, x2, y3, x3, z0)) {
            case PARTIAL_FLAG -> partialFace2(
                cache.computeNorthBlock(level, state, pos),
                cache.computeNorth(level, state, pos),
                z0,
                y0,
                x0,
                y1,
                x1,
                y2,
                x2,
                y3,
                x3,
                output
            );
            case PARTIAL_LIGHT_FACE_FLAG ->
                partialFace2(cache.computeNorthBlock(level, state, pos), y0, x0, y1, x1, y2, x2, y3, x3, output);
            case CUBIC_FLAG_0 -> fullFace0(
                cache.computeNorthBlock(level, state, pos),
                cache.computeNorth(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_0 -> fullFace0(cache.computeNorthBlock(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFace1(
                cache.computeNorthBlock(level, state, pos),
                cache.computeNorth(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_1 -> fullFace1(cache.computeNorthBlock(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFace2(
                cache.computeNorthBlock(level, state, pos),
                cache.computeNorth(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_2 -> fullFace2(cache.computeNorthBlock(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFace3(
                cache.computeNorthBlock(level, state, pos),
                cache.computeNorth(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_3 -> fullFace3(cache.computeNorthBlock(level, state, pos), output);
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
        float y0 = p0.y(), y1 = p1.y(), y2 = p2.y(), y3 = p3.y();
        float x0 = p0.x(), x1 = p1.x(), x2 = p2.x(), x3 = p3.x();
        switch (computeFlag(y0, x0, y1, x1, y2, x2, y3, x3, z0)) {
            case PARTIAL_FLAG -> partialFace2(
                cache.computeNorthBlockShade(level, state, pos),
                cache.computeNorthShade(level, state, pos),
                z0,
                y0,
                x0,
                y1,
                x1,
                y2,
                x2,
                y3,
                x3,
                output
            );
            case PARTIAL_LIGHT_FACE_FLAG ->
                partialFace2(cache.computeNorthBlockShade(level, state, pos), y0, x0, y1, x1, y2, x2, y3, x3, output);
            case CUBIC_FLAG_0 -> fullFace0(
                cache.computeNorthBlockShade(level, state, pos),
                cache.computeNorthShade(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_0 -> fullFace0(cache.computeNorthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_1 -> fullFace1(
                cache.computeNorthBlockShade(level, state, pos),
                cache.computeNorthShade(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_1 -> fullFace1(cache.computeNorthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_2 -> fullFace2(
                cache.computeNorthBlockShade(level, state, pos),
                cache.computeNorthShade(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_2 -> fullFace2(cache.computeNorthBlockShade(level, state, pos), output);
            case CUBIC_FLAG_3 -> fullFace3(
                cache.computeNorthBlockShade(level, state, pos),
                cache.computeNorthShade(level, state, pos),
                z0,
                output
            );
            case CUBIC_LIGHT_FACE_FLAG_3 -> fullFace3(cache.computeNorthBlockShade(level, state, pos), output);
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
        irregularFace(0, level, state, pos, output, cache, z0, p0.y(), p0.x());
        irregularFace(1, level, state, pos, output, cache, z1, p1.y(), p1.x());
        irregularFace(2, level, state, pos, output, cache, z2, p2.y(), p2.x());
        irregularFace(3, level, state, pos, output, cache, z3, p3.y(), p3.x());
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
        irregularShadeFace(0, level, state, pos, output, cache, z0, p0.y(), p0.x());
        irregularShadeFace(1, level, state, pos, output, cache, z1, p1.y(), p1.x());
        irregularShadeFace(2, level, state, pos, output, cache, z2, p2.y(), p2.x());
        irregularShadeFace(3, level, state, pos, output, cache, z3, p3.y(), p3.x());
    }

    private static void irregularFace(
        int i,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        QuadInstance output,
        AoFaceDataCache cache,
        float z,
        float y,
        float x
    ) {
        float w = clamp(z), u1 = clamp(y), v1 = clamp(x);
        float u0 = 1 - u1, v0 = 1 - v1;
        if (w < 0.00001f) {
            write(i, cache.computeNorthBlock(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeNorth(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeNorthBlock(level, state, pos);
            AoFaceData f1 = cache.computeNorth(level, state, pos);
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
        float y,
        float x
    ) {
        float w = clamp(z), u1 = clamp(y), v1 = clamp(x);
        float u0 = 1 - u1, v0 = 1 - v1;
        if (w < 0.00001f) {
            write(i, cache.computeNorthBlockShade(level, state, pos), u0, v0, u1, v1, output);
        } else if (w > 0.99999f) {
            write(i, cache.computeNorthShade(level, state, pos), u0, v0, u1, v1, output);
        } else {
            AoFaceData f0 = cache.computeNorthBlockShade(level, state, pos);
            AoFaceData f1 = cache.computeNorthShade(level, state, pos);
            write(i, f0, f1, w, u0, v0, u1, v1, output);
        }
    }
}
