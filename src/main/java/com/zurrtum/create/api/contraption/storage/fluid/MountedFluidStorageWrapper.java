package com.zurrtum.create.api.contraption.storage.fluid;

import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.infrastructure.fluids.CombinedTankWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.util.math.BlockPos;

/**
 * Wrapper around many MountedFluidStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedFluidStorageWrapper extends CombinedTankWrapper {
    public final ImmutableMap<BlockPos, MountedFluidStorage> storages;

    public MountedFluidStorageWrapper(ImmutableMap<BlockPos, MountedFluidStorage> storages) {
        super(storages.values().toArray(FluidInventory[]::new));
        this.storages = storages;
    }
}
