package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import net.minecraft.block.entity.BlockEntity;

public interface BlockEntityVisualizer<T extends BlockEntity> {
    BlockEntityVisual<? super T> createVisual(VisualizationContext var1, T var2, float var3);

    boolean skipVanillaRender(T var1);
}
