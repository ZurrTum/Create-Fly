package com.zurrtum.create.impl.contraption.storage;

import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;

public class FallbackMountedStorageType extends SimpleMountedStorageType<FallbackMountedStorage> {
    public FallbackMountedStorageType() {
        super(FallbackMountedStorage.CODEC);
    }

    @Override
    protected FallbackMountedStorage createStorage(Inventory handler) {
        return new FallbackMountedStorage(handler);
    }

    @Override
    protected Inventory getHandler(World level, BlockEntity be) {
        Inventory handler = super.getHandler(level, be);
        return handler != null && FallbackMountedStorage.isValid(handler) ? handler : null;
    }
}
