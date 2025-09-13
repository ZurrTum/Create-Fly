package com.zurrtum.create.client.vanillin.compose;

import com.zurrtum.create.client.flywheel.api.visual.Visual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;

public interface VisualElement<T, C> {
    Visual create(VisualizationContext ctx, T entity, float partialTick, C config);

    interface Unit<T> extends VisualElement<T, Object> {
        @Override
        default Visual create(VisualizationContext ctx, T entity, float partialTick, Object config) {
            return create(ctx, entity, partialTick);
        }

        Visual create(VisualizationContext ctx, T entity, float partialTick);
    }
}
