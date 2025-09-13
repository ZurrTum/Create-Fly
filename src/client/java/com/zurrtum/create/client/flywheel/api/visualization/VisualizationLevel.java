package com.zurrtum.create.client.flywheel.api.visualization;

import net.minecraft.world.WorldAccess;

/**
 * A marker interface custom levels can override to indicate
 * that block entities and entities inside the level should
 * render with Flywheel.
 * <br>
 * {@link net.minecraft.client.MinecraftClient#world Minecraft#level} is special cased and will support Flywheel by default.
 */
public interface VisualizationLevel extends WorldAccess {
    default boolean supportsVisualization() {
        return true;
    }
}
