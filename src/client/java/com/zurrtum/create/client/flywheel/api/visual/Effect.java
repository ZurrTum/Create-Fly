package com.zurrtum.create.client.flywheel.api.visual;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import net.minecraft.world.level.LevelAccessor;

/**
 * An effect is not attached to any formal game object, but allows you to hook into
 * flywheel's systems to render things. They're closely analogous to particles but
 * without any built in support for networking.
 */
public interface Effect {
    LevelAccessor level();

    /**
     * Create a visual that will be keyed by this effect object.
     *
     * @param ctx The visualization context.
     * @return An arbitrary EffectVisual.
     */
    EffectVisual<?> visualize(VisualizationContext ctx, float partialTick);
}
