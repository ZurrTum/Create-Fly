package com.zurrtum.create.client.flywheel.impl.extension;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityTypeExtension<T extends BlockEntity> {
    @Nullable BlockEntityVisualizer<? super T> flywheel$getVisualizer();

    void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer);
}
