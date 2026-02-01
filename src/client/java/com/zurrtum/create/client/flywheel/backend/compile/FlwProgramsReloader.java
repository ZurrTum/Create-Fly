package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.backend.NoiseTextures;
import com.zurrtum.create.foundation.utility.CreateResourceReloader;
import net.minecraft.server.packs.resources.ResourceManager;

public final class FlwProgramsReloader extends CreateResourceReloader {
    public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

    private FlwProgramsReloader() {
        super("flywheel");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        FlwPrograms.reload(manager);
        NoiseTextures.reload(manager);
    }
}
