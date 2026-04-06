package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Holds all direct references to the Sodium API. This class is only loaded when Sodium is
 * present ({@link SodiumCompat#ACTIVE} is true), so the mod can start without Sodium on the
 * classpath without triggering a {@link NoClassDefFoundError}.
 */
final class SodiumCompatInternals {
    private SodiumCompatInternals() {
    }

    static <T extends BlockEntity> Object addPredicate(BlockEntityType<T> type) {
        BlockEntityRenderPredicate<T> predicate = (getter, pos, be) -> !VisualizationHelper.tryAddBlockEntity(be);
        BlockEntityRenderHandler.instance().addRenderPredicate(type, predicate);
        return predicate;
    }

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> void removePredicate(BlockEntityType<T> type, Object predicate) {
        BlockEntityRenderHandler.instance().removeRenderPredicate(type, (BlockEntityRenderPredicate<T>) predicate);
    }
}
