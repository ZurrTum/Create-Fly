package com.zurrtum.create.client.flywheel.api.internal;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.layout.LayoutBuilder;
import com.zurrtum.create.client.flywheel.api.registry.IdRegistry;
import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.FlwApiLinkImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public interface FlwApiLink {
    FlwApiLink INSTANCE = new FlwApiLinkImpl();

    <T> IdRegistry<T> createIdRegistry();

    Backend getCurrentBackend();

    boolean isBackendOn();

    Backend getOffBackend();

    Backend getDefaultBackend();

    LayoutBuilder createLayoutBuilder();

    boolean supportsVisualization(@Nullable LevelAccessor level);

    @Nullable VisualizationManager getVisualizationManager(@Nullable LevelAccessor level);

    VisualizationManager getVisualizationManagerOrThrow(@Nullable LevelAccessor level);

    <T extends BlockEntity> @Nullable BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type);

    <T extends Entity> @Nullable EntityVisualizer<? super T> getVisualizer(EntityType<T> type);

    <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, @Nullable BlockEntityVisualizer<? super T> visualizer);

    <T extends Entity> void setVisualizer(EntityType<T> type, @Nullable EntityVisualizer<? super T> visualizer);
}
