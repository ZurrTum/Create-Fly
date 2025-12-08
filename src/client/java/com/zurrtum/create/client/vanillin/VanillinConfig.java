package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.client.vanillin.config.*;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.vanillin.Vanillin.MOD_ID;


public class VanillinConfig {
    public static final String VANILLIN_OVERRIDES = "vanillin:overrides";
    private static ModOverrides overrides;
    private static CClient client;

    public static ModOverrides modOverrides() {
        var blockEntities = new ArrayList<VisualOverride>();
        var entities = new ArrayList<VisualOverride>();

        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();
            var modid = meta.getId();

            if (meta.containsCustomValue(VANILLIN_OVERRIDES)) {
                CustomValue overridesValue = meta.getCustomValue(VANILLIN_OVERRIDES);

                if (overridesValue.getType() != CustomValue.CvType.OBJECT) {
                    Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override options with an invalid value, ignoring", modid);
                    continue;
                }

                var overrides = overridesValue.getAsObject();

                readSection(blockEntities, modid, overrides, "block_entities", "block entity");
                readSection(entities, modid, overrides, "entities", "entity");
            }
        }

        return new ModOverrides(blockEntities, entities);
    }

    private static void readSection(List<VisualOverride> dst, String modid, CustomValue.CvObject overrides, String sectionName, String singular) {
        if (!overrides.containsKey(sectionName)) {
            return;
        }

        var section = overrides.get(sectionName);

        if (section.getType() != CustomValue.CvType.OBJECT) {
            Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} with an invalid value, ignoring", modid, sectionName);
            return;
        }

        for (Map.Entry<String, CustomValue> entry : section.getAsObject()) {
            var value = entry.getValue();
            var key = entry.getKey();
            if (value.getType() != CustomValue.CvType.STRING) {
                Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} '{}' with an invalid value, ignoring", modid, singular, key);
                continue;
            }

            var valueString = value.getAsString();

            var parsed = VisualOverrideValue.parse(valueString);

            if (parsed == null) {
                Vanillin.CONFIG_LOGGER.warn(
                    "Mod '{}' attempted to override {} '{}' with an invalid value '{}', ignoring",
                    modid,
                    singular,
                    key,
                    valueString
                );
                continue;
            }

            dst.add(new VisualOverride(key, modid, parsed));
        }
    }

    public static CClient client() {
        return client;
    }

    public static ModOverrides overrides() {
        return overrides;
    }

    public static void register() {
        client = Builder.create(CClient::new, MOD_ID, "client", true);
        overrides = modOverrides();
    }

    public static void apply(Configurator configurator) {
        var blockEntities = client.blockEntities;
        var blockEntityOverrides = overrides.blockEntities();
        for (Configurator.ConfiguredVisual configured : configurator.blockEntities.values()) {
            apply(configured, blockEntities, blockEntityOverrides);
        }

        var entities = client.entities;
        var entityOverrides = overrides.entities();
        for (Configurator.ConfiguredVisual configured : configurator.entities.values()) {
            apply(configured, entities, entityOverrides);
        }
    }

    private static void apply(
        Configurator.ConfiguredVisual configured,
        Map<String, ConfigBase.ConfigEnum<VisualConfigValue>> config,
        Map<String, List<VisualOverride>> overrides
    ) {
        String key = configured.configKey();
        VisualConfigValue enabled = config.get(key).get();
        configured.set(enabled, overrides.get(key));
    }
}
