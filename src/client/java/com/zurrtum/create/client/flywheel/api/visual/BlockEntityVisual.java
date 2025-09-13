package com.zurrtum.create.client.flywheel.api.visual;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BlockEntityVisual<T extends BlockEntity> extends Visual {
    void collectCrumblingInstances(Consumer<@Nullable Instance> var1);
}
