package com.zurrtum.create.client.flywheel.impl.visualization.storage;

import com.zurrtum.create.client.flywheel.api.visual.EntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityStorage extends Storage<Entity> {
    @Override
    protected EntityVisual<?> createRaw(VisualizationContext context, Entity obj, float partialTick) {
        var visualizer = VisualizationHelper.getVisualizer(obj);
        if (visualizer == null) {
            return null;
        }

        return visualizer.createVisual(context, obj, partialTick);
    }

    @Override
    public boolean willAccept(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        if (!VisualizationHelper.canVisualize(entity)) {
            return false;
        }

        World level = entity.getEntityWorld();
        return level != null;
    }
}
