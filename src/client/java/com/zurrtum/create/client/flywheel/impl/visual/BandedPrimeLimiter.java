package com.zurrtum.create.client.flywheel.impl.visual;

import net.minecraft.util.math.MathHelper;

public class BandedPrimeLimiter implements DistanceUpdateLimiterImpl {
    // 1 followed by the prime numbers
    private static final int[] DIVISOR_SEQUENCE = new int[]{1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};

    private int tickCount = 0;

    @Override
    public void tick() {
        tickCount++;
    }

    @Override
    public boolean shouldUpdate(double distanceSquared) {
        return (tickCount % getUpdateDivisor(distanceSquared)) == 0;
    }

    protected int getUpdateDivisor(double distanceSquared) {
        int dSq = MathHelper.ceil(distanceSquared);

        int i = (dSq / 2048);

        return DIVISOR_SEQUENCE[MathHelper.clamp(i, 0, DIVISOR_SEQUENCE.length - 1)];
    }
}
