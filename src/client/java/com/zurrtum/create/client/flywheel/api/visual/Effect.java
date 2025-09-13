package com.zurrtum.create.client.flywheel.api.visual;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import net.minecraft.world.WorldAccess;

public interface Effect {
    WorldAccess level();

    EffectVisual<?> visualize(VisualizationContext var1, float var2);
}
