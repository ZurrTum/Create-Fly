package com.zurrtum.create.client.flywheel.lib.visual;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.lib.task.RunnablePlan;

public interface SimpleDynamicVisual extends DynamicVisual {
    /**
     * Called every frame.
     * <br>
     * The implementation is free to parallelize calls to this method.
     * You must ensure proper synchronization if you need to mutate anything outside this visual.
     * <br>
     * This method and {@link SimpleTickableVisual#tick} will never be called simultaneously.
     * <br>
     * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
     */
    void beginFrame(Context ctx);

    @Override
    default Plan<Context> planFrame() {
        return RunnablePlan.of(this::beginFrame);
    }
}
