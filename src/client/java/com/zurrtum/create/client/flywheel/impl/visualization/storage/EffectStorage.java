package com.zurrtum.create.client.flywheel.impl.visualization.storage;

import com.zurrtum.create.client.flywheel.api.visual.Effect;
import com.zurrtum.create.client.flywheel.api.visual.EffectVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;

public class EffectStorage extends Storage<Effect> {
    @Override
    protected EffectVisual<?> createRaw(VisualizationContext visualizationContext, Effect obj, float partialTick) {
        return obj.visualize(visualizationContext, partialTick);
    }

    @Override
    public boolean willAccept(Effect obj) {
        return true;
    }
}
