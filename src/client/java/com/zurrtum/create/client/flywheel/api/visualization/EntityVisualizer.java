package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.visual.EntityVisual;
import net.minecraft.entity.Entity;

public interface EntityVisualizer<T extends Entity> {
    EntityVisual<? super T> createVisual(VisualizationContext var1, T var2, float var3);

    boolean skipVanillaRender(T var1);
}
