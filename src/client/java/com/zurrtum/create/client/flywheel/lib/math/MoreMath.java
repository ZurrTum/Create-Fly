package com.zurrtum.create.client.flywheel.lib.math;

public final class MoreMath {
    public static final float SQRT_3_OVER_2 = (float) (Math.sqrt((double) 3.0F) / (double) 2.0F);

    private MoreMath() {
    }

    public static int align32(int size) {
        return size + 31 & -32;
    }

    public static int align16(int size) {
        return size + 15 & -16;
    }

    public static int align4(int size) {
        return size + 3 & -4;
    }

    public static int alignPot(int size, int to) {
        return size + (to - 1) & ~(to - 1);
    }

    public static int ceilingDiv(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    public static long ceilingDiv(long numerator, long denominator) {
        return (numerator + denominator - 1L) / denominator;
    }

    public static long ceilLong(double d) {
        return (long) Math.ceil(d);
    }

    public static long ceilLong(float f) {
        return (long) Math.ceil((double) f);
    }
}
