package com.zurrtum.create.client.vanillin.config;

import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.client.vanillin.VanillaVisuals;

import java.util.HashMap;
import java.util.Map;

public class CClient extends ConfigBase {
    public final ConfigGroup blockEntitiesGroup = group(1, "blockEntities");
    public final Map<String, ConfigEnum<VisualConfigValue>> blockEntities = new HashMap<>();

    {
        for (Configurator.ConfiguredBlockEntity<?> config : VanillaVisuals.CONFIGURATOR.blockEntities.values()) {
            String key = config.configKey();
            blockEntities.put(key, e(VisualConfigValue.DEFAULT, key));
        }
    }

    public final ConfigGroup entitiesGroup = group(1, "entities");
    public final Map<String, ConfigEnum<VisualConfigValue>> entities = new HashMap<>();

    {
        for (Configurator.ConfiguredEntity<?> config : VanillaVisuals.CONFIGURATOR.entities.values()) {
            String key = config.configKey();
            entities.put(key, e(VisualConfigValue.DEFAULT, key));
        }
    }

    @Override
    public String getName() {
        return "client";
    }
}
