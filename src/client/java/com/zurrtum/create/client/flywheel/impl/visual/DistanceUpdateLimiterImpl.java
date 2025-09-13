package com.zurrtum.create.client.flywheel.impl.visual;

public interface DistanceUpdateLimiterImpl extends DistanceUpdateLimiter {
    /**
     * Call this before every update.
     */
    void tick();
}
