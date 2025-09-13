package com.zurrtum.create.api.contraption.storage.item.chest;

import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;

public class ChestMountedStorageType extends SimpleMountedStorageType<ChestMountedStorage> {
    public ChestMountedStorageType() {
        super(ChestMountedStorage.CODEC);
    }

    @Override
    protected Inventory getHandler(World level, BlockEntity be) {
        return be instanceof Inventory container ? container : null;
    }

    @Override
    protected ChestMountedStorage createStorage(Inventory handler) {
        return new ChestMountedStorage(handler);
    }
}
