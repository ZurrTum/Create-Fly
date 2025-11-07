package com.zurrtum.create.client.flywheel.impl.visual;

import com.zurrtum.create.client.flywheel.api.visual.DistanceUpdateLimiter;

public interface DistanceUpdateLimiterImpl extends DistanceUpdateLimiter {
    /**
     * Call this before every update.
     */
    void tick();
}
