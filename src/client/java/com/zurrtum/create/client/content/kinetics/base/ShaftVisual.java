package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

public class ShaftVisual<T extends KineticBlockEntity> extends SingleAxisRotatingVisual<T> {
    public ShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT));
    }
}
