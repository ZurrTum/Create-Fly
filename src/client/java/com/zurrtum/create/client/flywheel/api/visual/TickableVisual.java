package com.zurrtum.create.client.flywheel.api.visual;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import org.jetbrains.annotations.ApiStatus;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the end of every tick.
 */
public interface TickableVisual extends Visual {
    /**
     * Invoked every tick.
     * <br>
     * The implementation is free to parallelize the invocation of this plan.
     * You must ensure proper synchronization if you need to mutate anything outside this visual.
     * <br>
     * This plan and the one returned by {@link DynamicVisual#planFrame} will never be invoked simultaneously.
     * <br>
     * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
     */
    Plan<Context> planTick();

    /**
     * The context passed to the tick plan.
     * <p>Currently this has no methods, it is reserved here for future use.</p>
     */
    @ApiStatus.NonExtendable
    interface Context {
    }
}
