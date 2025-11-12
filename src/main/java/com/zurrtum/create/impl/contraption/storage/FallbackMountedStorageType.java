package com.zurrtum.create.impl.contraption.storage;

import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FallbackMountedStorageType extends SimpleMountedStorageType<FallbackMountedStorage> {
    public FallbackMountedStorageType() {
        super(FallbackMountedStorage.CODEC);
    }

    @Override
    protected FallbackMountedStorage createStorage(Container handler) {
        return new FallbackMountedStorage(handler);
    }

    @Override
    protected Container getHandler(Level level, BlockEntity be) {
        Container handler = super.getHandler(level, be);
        return handler != null && FallbackMountedStorage.isValid(handler) ? handler : null;
    }
}
