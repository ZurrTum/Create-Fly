package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.impl.FlwImpl;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public final class SodiumCompat {
    public static final boolean ACTIVE = CompatMod.SODIUM.isLoaded;

    static {
        if (ACTIVE) {
            FlwImpl.LOGGER.debug("Detected Sodium");
        }
    }

    private SodiumCompat() {
    }

    @Nullable
    public static <T extends BlockEntity> Object onSetBlockEntityVisualizer(
        BlockEntityType<T> type,
        @Nullable BlockEntityVisualizer<? super T> oldVisualizer,
        @Nullable BlockEntityVisualizer<? super T> newVisualizer,
        @Nullable Object predicate
    ) {
        if (!ACTIVE) {
            return null;
        }

        if (oldVisualizer == null && newVisualizer != null) {
            if (predicate != null) {
                throw new IllegalArgumentException("Sodium predicate must be null when old visualizer is null");
            }

            return SodiumCompatInternals.addPredicate(type);
        } else if (oldVisualizer != null && newVisualizer == null) {
            SodiumCompatInternals.removePredicate(type, predicate);
            return null;
        }

        return predicate;
    }
}