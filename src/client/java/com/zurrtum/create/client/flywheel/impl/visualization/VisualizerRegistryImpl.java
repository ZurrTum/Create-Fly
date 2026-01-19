package com.zurrtum.create.client.flywheel.impl.visualization;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import com.zurrtum.create.client.flywheel.impl.extension.BlockEntityTypeExtension;
import com.zurrtum.create.client.flywheel.impl.extension.EntityTypeExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unchecked")
public final class VisualizerRegistryImpl {
    @Nullable
    public static <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
        return ((BlockEntityTypeExtension<T>) type).flywheel$getVisualizer();
    }

    @Nullable
    public static <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
        return ((EntityTypeExtension<T>) type).flywheel$getVisualizer();
    }

    public static <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, @Nullable BlockEntityVisualizer<? super T> visualizer) {
        ((BlockEntityTypeExtension<T>) type).flywheel$setVisualizer(visualizer);
    }

    public static <T extends Entity> void setVisualizer(EntityType<T> type, @Nullable EntityVisualizer<? super T> visualizer) {
        ((EntityTypeExtension<T>) type).flywheel$setVisualizer(visualizer);
    }

    private VisualizerRegistryImpl() {
    }
}
