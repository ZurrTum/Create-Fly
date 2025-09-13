package com.zurrtum.create.content.contraptions.behaviour.dispenser.storage;

import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import net.minecraft.inventory.Inventory;

public class DispenserMountedStorageType extends SimpleMountedStorageType<DispenserMountedStorage> {
    public DispenserMountedStorageType() {
        super(DispenserMountedStorage.CODEC);
    }

    @Override
    protected DispenserMountedStorage createStorage(Inventory handler) {
        return new DispenserMountedStorage(handler);
    }
}
