package com.zurrtum.create.client.content.contraptions.wrench;

import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationLevel;
import net.minecraft.world.World;

public class NonVisualizationLevel extends WrappedLevel implements VisualizationLevel {
    public NonVisualizationLevel(World level) {
        super(level);
    }

    @Override
    public boolean supportsVisualization() {
        return false;
    }
}
