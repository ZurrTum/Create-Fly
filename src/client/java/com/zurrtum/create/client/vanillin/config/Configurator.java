package com.zurrtum.create.client.vanillin.config;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizerRegistry;
import com.zurrtum.create.client.vanillin.Vanillin;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Configurator {
    public final Map<BlockEntityType<?>, ConfiguredBlockEntity<?>> blockEntities = new LinkedHashMap<>();
    public final Map<EntityType<?>, ConfiguredEntity<?>> entities = new LinkedHashMap<>();

    public <T extends BlockEntity> void register(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
        blockEntities.put(type, new ConfiguredBlockEntity<>(type, visualizer, enabledByDefault));
    }

    public <T extends Entity> void register(EntityType<T> type, EntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
        entities.put(type, new ConfiguredEntity<>(type, visualizer, enabledByDefault));
    }

    public static abstract class ConfiguredVisual {
        private final boolean enabledByDefault;

        protected ConfiguredVisual(boolean enabledByDefault) {
            this.enabledByDefault = enabledByDefault;
        }

        public void set(VisualConfigValue configValue, @Nullable List<VisualOverride> overrides) {
            if (configValue == VisualConfigValue.DISABLE) {
                disable();
            } else if (configValue == VisualConfigValue.FORCE_ENABLE) {
                enable();
                maybeWarnEnabledDespiteOverrides(overrides);
            } else if (configValue == VisualConfigValue.DEFAULT) {
                if (disableAndWarnDueToOverrides(overrides)) {
                    disable();
                } else {
                    if (enabledByDefault) {
                        enable();
                    } else {
                        disable();
                    }
                }
            }
        }

        private boolean disableAndWarnDueToOverrides(@Nullable List<VisualOverride> overrides) {
            if (overrides == null || overrides.isEmpty()) {
                return false;
            }

            var modIds = disablingModIds(overrides);

            if (modIds.isEmpty()) {
                return false;
            } else {
                Vanillin.CONFIG_LOGGER.warn("Disabling {} visual due to overrides from mods: {}", configKey(), String.join(", ", modIds));
                return true;
            }
        }

        private void maybeWarnEnabledDespiteOverrides(@Nullable List<VisualOverride> overrides) {
            if (overrides == null || overrides.isEmpty()) {
                return;
            }

            var modIds = disablingModIds(overrides);

            if (!modIds.isEmpty()) {
                Vanillin.CONFIG_LOGGER.warn("Enabling {} visual despite overrides from mods: {}", configKey(), String.join(", ", modIds));
            }
        }

        public abstract String configKey();

        protected abstract void enable();

        protected abstract void disable();

        private static List<String> disablingModIds(List<VisualOverride> overrides) {
            List<String> out = new ArrayList<>();

            for (VisualOverride override : overrides) {
                if (override.value() == VisualOverrideValue.DISABLE) {
                    out.add(override.modId());
                }
            }
            return out;
        }
    }

    public static class ConfiguredBlockEntity<T extends BlockEntity> extends ConfiguredVisual {
        public final BlockEntityType<T> type;
        public final BlockEntityVisualizer<? super T> visualizer;

        private ConfiguredBlockEntity(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
            super(enabledByDefault);
            this.type = type;
            this.visualizer = visualizer;
        }

        @Override
        public String configKey() {
            return Registries.BLOCK_ENTITY_TYPE.getId(type).toString();
        }

        @Override
        protected void enable() {
            VisualizerRegistry.setVisualizer(type, visualizer);
        }

        @Override
        protected void disable() {
            VisualizerRegistry.setVisualizer(type, null);
        }
    }

    public static class ConfiguredEntity<T extends Entity> extends ConfiguredVisual {
        public final EntityType<T> type;
        public final EntityVisualizer<? super T> visualizer;

        private ConfiguredEntity(EntityType<T> type, EntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
            super(enabledByDefault);
            this.type = type;
            this.visualizer = visualizer;
        }

        @Override
        public String configKey() {
            return Registries.ENTITY_TYPE.getId(type).toString();
        }

        @Override
        protected void enable() {
            VisualizerRegistry.setVisualizer(type, visualizer);
        }

        @Override
        protected void disable() {
            VisualizerRegistry.setVisualizer(type, null);
        }
    }
}
