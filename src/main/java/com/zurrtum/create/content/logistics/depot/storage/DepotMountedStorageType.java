package com.zurrtum.create.content.logistics.depot.storage;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DepotMountedStorageType extends MountedItemStorageType<DepotMountedStorage> {
    public DepotMountedStorageType() {
        super(DepotMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public DepotMountedStorage mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof DepotBlockEntity depot) {
            return DepotMountedStorage.fromDepot(depot);
        }

        return null;
    }
}
