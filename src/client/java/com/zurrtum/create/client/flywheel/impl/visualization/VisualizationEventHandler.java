package com.zurrtum.create.client.flywheel.impl.visualization;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public final class VisualizationEventHandler {
    private VisualizationEventHandler() {
    }

    public static void onClientTick(MinecraftClient minecraft, World level) {
        // The game won't be paused in the tick event, but let's make sure there's a player.
        if (minecraft.player == null) {
            return;
        }

        VisualizationManagerImpl manager = VisualizationManagerImpl.get(level);
        if (manager == null) {
            return;
        }

        manager.tick();
    }

    public static void onEntityJoinLevel(World level, Entity entity) {
        VisualizationManager manager = VisualizationManager.get(level);
        if (manager == null) {
            return;
        }

        manager.entities().queueAdd(entity);
    }

    public static void onEntityLeaveLevel(World level, Entity entity) {
        VisualizationManager manager = VisualizationManager.get(level);
        if (manager == null) {
            return;
        }

        manager.entities().queueRemove(entity);
    }
}
