package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.backend.NoiseTextures;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

public final class FlwProgramsReloader implements SynchronousResourceReloader {
    public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

    private FlwProgramsReloader() {
    }

    @Override
    public void reload(ResourceManager manager) {
        FlwPrograms.reload(manager);
        NoiseTextures.reload(manager);
    }
}
