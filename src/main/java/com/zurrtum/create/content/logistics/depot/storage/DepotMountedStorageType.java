package com.zurrtum.create.content.logistics.depot.storage;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DepotMountedStorageType extends MountedItemStorageType<DepotMountedStorage> {
    public DepotMountedStorageType() {
        super(DepotMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public DepotMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof DepotBlockEntity depot) {
            return DepotMountedStorage.fromDepot(depot);
        }

        return null;
    }
}
