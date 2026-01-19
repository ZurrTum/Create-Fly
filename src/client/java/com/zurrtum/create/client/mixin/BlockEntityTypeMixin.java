package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.impl.compat.SodiumCompat;
import com.zurrtum.create.client.flywheel.impl.extension.BlockEntityTypeExtension;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityType.class)
abstract class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeExtension<T> {
    @Unique
    @Nullable
    private BlockEntityVisualizer<? super T> flywheel$visualizer;

    @Unique
    @Nullable
    private Object flywheel$sodiumPredicate;

    @Override
    @Nullable
    public BlockEntityVisualizer<? super T> flywheel$getVisualizer() {
        return flywheel$visualizer;
    }

    @Override
    public void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer) {
        if (SodiumCompat.ACTIVE) {
            flywheel$sodiumPredicate = SodiumCompat.onSetBlockEntityVisualizer(
                (BlockEntityType<T>) (Object) this,
                flywheel$visualizer,
                visualizer,
                flywheel$sodiumPredicate
            );
        }

        flywheel$visualizer = visualizer;
    }
}
