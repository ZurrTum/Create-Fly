package com.zurrtum.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedItemStorageWrapper extends CombinedInvWrapper {
    public final ImmutableMap<BlockPos, MountedItemStorage> storages;

    public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
        super(storages.values().toArray(Inventory[]::new));
        this.storages = storages;
    }
}
