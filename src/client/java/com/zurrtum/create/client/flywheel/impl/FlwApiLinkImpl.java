package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import com.zurrtum.create.client.flywheel.api.layout.LayoutBuilder;
import com.zurrtum.create.client.flywheel.api.registry.IdRegistry;
import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.layout.LayoutBuilderImpl;
import com.zurrtum.create.client.flywheel.impl.registry.IdRegistryImpl;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationManagerImpl;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizerRegistryImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class FlwApiLinkImpl implements FlwApiLink {
    @Override
    public <T> IdRegistry<T> createIdRegistry() {
        return new IdRegistryImpl<>();
    }

    @Override
    public Backend getCurrentBackend() {
        return BackendManagerImpl.currentBackend();
    }

    @Override
    public boolean isBackendOn() {
        return BackendManagerImpl.isBackendOn();
    }

    @Override
    public Backend getOffBackend() {
        return BackendManagerImpl.OFF_BACKEND;
    }

    @Override
    public Backend getDefaultBackend() {
        return BackendManagerImpl.defaultBackend();
    }

    @Override
    public LayoutBuilder createLayoutBuilder() {
        return new LayoutBuilderImpl();
    }

    @Override
    public boolean supportsVisualization(@Nullable WorldAccess level) {
        return VisualizationManagerImpl.supportsVisualization(level);
    }

    @Override
    @Nullable
    public VisualizationManager getVisualizationManager(@Nullable WorldAccess level) {
        return VisualizationManagerImpl.get(level);
    }

    @Override
    public VisualizationManager getVisualizationManagerOrThrow(@Nullable WorldAccess level) {
        return VisualizationManagerImpl.getOrThrow(level);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
        return VisualizerRegistryImpl.getVisualizer(type);
    }

    @Override
    @Nullable
    public <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
        return VisualizerRegistryImpl.getVisualizer(type);
    }

    @Override
    public <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, @Nullable BlockEntityVisualizer<? super T> visualizer) {
        VisualizerRegistryImpl.setVisualizer(type, visualizer);
    }

    @Override
    public <T extends Entity> void setVisualizer(EntityType<T> type, @Nullable EntityVisualizer<? super T> visualizer) {
        VisualizerRegistryImpl.setVisualizer(type, visualizer);
    }
}
