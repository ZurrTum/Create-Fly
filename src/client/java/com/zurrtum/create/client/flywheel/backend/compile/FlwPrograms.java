package com.zurrtum.create.client.flywheel.backend.compile;

import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import static com.zurrtum.create.client.flywheel.impl.Flywheel.MOD_ID;

public final class FlwPrograms {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "/backend/shaders");

    private static final ResourceLocation COMPONENTS_HEADER_FRAG = ResourceUtil.rl("internal/components_header.frag");

    public static ShaderSources SOURCES;

    private FlwPrograms() {
    }

    static void reload(ResourceManager resourceManager) {
        // Reset the programs in case the ubershader load fails.
        InstancingPrograms.setInstance(null);
        IndirectPrograms.setInstance(null);

        var sources = new ShaderSources(resourceManager);
        SOURCES = sources;

        var fragmentComponentsHeader = sources.get(COMPONENTS_HEADER_FRAG);

        List<SourceComponent> vertexComponents = List.of();
        List<SourceComponent> fragmentComponents = List.of(fragmentComponentsHeader);

        InstancingPrograms.reload(sources, vertexComponents, fragmentComponents);
        IndirectPrograms.reload(sources, vertexComponents, fragmentComponents);
    }
}
