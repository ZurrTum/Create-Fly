package com.zurrtum.create.client.flywheel.api.internal;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.layout.LayoutBuilder;
import com.zurrtum.create.client.flywheel.api.registry.IdRegistry;
import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.FlwApiLinkImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface FlwApiLink {
    FlwApiLink INSTANCE = new FlwApiLinkImpl();

    <T> IdRegistry<T> createIdRegistry();

    Backend getCurrentBackend();

    boolean isBackendOn();

    Backend getOffBackend();

    Backend getDefaultBackend();

    LayoutBuilder createLayoutBuilder();

    boolean supportsVisualization(@Nullable WorldAccess var1);

    @Nullable VisualizationManager getVisualizationManager(@Nullable WorldAccess var1);

    VisualizationManager getVisualizationManagerOrThrow(@Nullable WorldAccess var1);

    <T extends BlockEntity> @Nullable BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> var1);

    <T extends Entity> @Nullable EntityVisualizer<? super T> getVisualizer(EntityType<T> var1);

    <T extends BlockEntity> void setVisualizer(BlockEntityType<T> var1, @Nullable BlockEntityVisualizer<? super T> var2);

    <T extends Entity> void setVisualizer(EntityType<T> var1, @Nullable EntityVisualizer<? super T> var2);
}
