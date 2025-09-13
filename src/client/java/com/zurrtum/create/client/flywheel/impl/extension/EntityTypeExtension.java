package com.zurrtum.create.client.flywheel.impl.extension;

import com.zurrtum.create.client.flywheel.api.visualization.EntityVisualizer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EntityTypeExtension<T extends Entity> {
    @Nullable EntityVisualizer<? super T> flywheel$getVisualizer();

    void flywheel$setVisualizer(@Nullable EntityVisualizer<? super T> visualizer);
}
