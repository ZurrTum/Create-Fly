package com.zurrtum.create.client.flywheel.api.visual;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.impl.visual.DistanceUpdateLimiter;
import net.minecraft.client.render.Camera;
import org.jetbrains.annotations.ApiStatus;
import org.joml.FrustumIntersection;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the start of a frame. By implementing {@link DynamicVisual}, an {@link Visual}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 */
public interface DynamicVisual extends Visual {
    /**
     * Invoked every frame.
     * <br>
     * The implementation is free to parallelize the invocation of this plan.
     * You must ensure proper synchronization if you need to mutate anything outside this visual.
     * <br>
     * This plan and the one returned by {@link TickableVisual#planTick} will never be invoked simultaneously.
     * <br>
     * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
     */
    Plan<Context> planFrame();

    /**
     * The context passed to the frame plan.
     */
    @ApiStatus.NonExtendable
    interface Context {
        Camera camera();

        FrustumIntersection frustum();

        float partialTick();

        DistanceUpdateLimiter limiter();
    }
}