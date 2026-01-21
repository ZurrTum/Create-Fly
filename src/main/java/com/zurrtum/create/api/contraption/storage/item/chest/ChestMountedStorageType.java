package com.zurrtum.create.api.contraption.storage.item.chest;

import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class ChestMountedStorageType extends SimpleMountedStorageType<ChestMountedStorage> {
    public ChestMountedStorageType() {
        super(ChestMountedStorage.CODEC);
    }

    @Override
    @Nullable
    protected Container getHandler(Level level, BlockEntity be) {
        return be instanceof Container container ? container : null;
    }

    @Override
    protected ChestMountedStorage createStorage(Container handler) {
        return new ChestMountedStorage(handler);
    }
}
