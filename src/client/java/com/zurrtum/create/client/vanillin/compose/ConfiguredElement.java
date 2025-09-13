package com.zurrtum.create.client.vanillin.compose;

import com.zurrtum.create.client.flywheel.api.visual.Visual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;

public interface ConfiguredElement<T> {
    Visual create(VisualizationContext ctx, T entity, float partialTick);

    boolean shouldVisualize(VisualizationContext ctx, T entity);
}
