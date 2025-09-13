package com.zurrtum.create.client.flywheel.lib.visual;

import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visual.Visual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class AbstractVisual implements Visual {
    /**
     * The visualization context used to construct this visual.
     * <br>
     * Useful for passing to child visuals.
     */
    protected final VisualizationContext visualizationContext;
    protected final World level;

    protected boolean deleted = false;

    public AbstractVisual(VisualizationContext ctx, World level, float partialTick) {
        this.visualizationContext = ctx;
        this.level = level;
    }

    @Override
    public void update(float partialTick) {
    }

    protected abstract void _delete();

    protected InstancerProvider instancerProvider() {
        return visualizationContext.instancerProvider();
    }

    protected Vec3i renderOrigin() {
        return visualizationContext.renderOrigin();
    }

    @Override
    public final void delete() {
        if (deleted) {
            return;
        }

        _delete();
        deleted = true;
    }
}
