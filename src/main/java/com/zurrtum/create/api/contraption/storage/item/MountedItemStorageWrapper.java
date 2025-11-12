package com.zurrtum.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedItemStorageWrapper extends CombinedInvWrapper {
    public final ImmutableMap<BlockPos, MountedItemStorage> storages;

    public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
        super(storages.values().toArray(Container[]::new));
        this.storages = storages;
    }
}
