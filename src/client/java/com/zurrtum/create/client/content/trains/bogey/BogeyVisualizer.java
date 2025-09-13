package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;

@FunctionalInterface
public interface BogeyVisualizer {
    BogeyVisual createVisual(VisualizationContext ctx, float partialTick, boolean inContraption);
}
