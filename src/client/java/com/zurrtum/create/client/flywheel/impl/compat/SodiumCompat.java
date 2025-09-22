package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.impl.FlwImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public final class SodiumCompat {
    public static final boolean ACTIVE = FabricLoader.getInstance().isModLoaded("sodium");

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

            return Internals.addPredicate(type);
        } else if (oldVisualizer != null && newVisualizer == null) {
            if (predicate == null) {
                throw new IllegalArgumentException("Sodium predicate must not be null when old visualizer is not null");
            }

            Internals.removePredicate(type, predicate);
            return null;
        }

        return predicate;
    }

    private static final class Internals {
        static <T extends BlockEntity> Object addPredicate(BlockEntityType<T> type) {
            BlockEntityRenderPredicate<T> predicate = (getter, pos, be) -> !VisualizationHelper.tryAddBlockEntity(be);
            BlockEntityRenderHandler.instance().addRenderPredicate(type, predicate);
            return predicate;
        }

        static <T extends BlockEntity> void removePredicate(BlockEntityType<T> type, Object predicate) {
            BlockEntityRenderHandler.instance().removeRenderPredicate(type, (BlockEntityRenderPredicate<T>) predicate);
        }
    }
}