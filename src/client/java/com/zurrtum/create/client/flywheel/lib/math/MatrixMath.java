package com.zurrtum.create.client.flywheel.lib.math;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class MatrixMath {
    private MatrixMath() {
    }

    public static float transformPositionX(Matrix4f matrix, float x, float y, float z) {
        return Math.fma(matrix.m00(), x, Math.fma(matrix.m10(), y, Math.fma(matrix.m20(), z, matrix.m30())));
    }

    public static float transformPositionY(Matrix4f matrix, float x, float y, float z) {
        return Math.fma(matrix.m01(), x, Math.fma(matrix.m11(), y, Math.fma(matrix.m21(), z, matrix.m31())));
    }

    public static float transformPositionZ(Matrix4f matrix, float x, float y, float z) {
        return Math.fma(matrix.m02(), x, Math.fma(matrix.m12(), y, Math.fma(matrix.m22(), z, matrix.m32())));
    }

    public static float transformNormalX(Matrix3f matrix, float x, float y, float z) {
        return Math.fma(matrix.m00(), x, Math.fma(matrix.m10(), y, matrix.m20() * z));
    }

    public static float transformNormalY(Matrix3f matrix, float x, float y, float z) {
        return Math.fma(matrix.m01(), x, Math.fma(matrix.m11(), y, matrix.m21() * z));
    }

    public static float transformNormalZ(Matrix3f matrix, float x, float y, float z) {
        return Math.fma(matrix.m02(), x, Math.fma(matrix.m12(), y, matrix.m22() * z));
    }
}
