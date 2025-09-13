package com.zurrtum.create.client.flywheel.impl.visual;

import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import net.minecraft.client.render.Camera;
import org.joml.FrustumIntersection;

public record DynamicVisualContextImpl(
    Camera camera, FrustumIntersection frustum, float partialTick, DistanceUpdateLimiter limiter
) implements DynamicVisual.Context {
}
