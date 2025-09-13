package com.zurrtum.create.impl.contraption.storage;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.inventory.Inventory;

import java.util.Optional;

/**
 * A fallback mounted storage impl that will try to be used when no type is
 * registered for a block. This requires that the mounted block provide an item handler
 * whose class is exactly {@link ItemStackHandler}.
 */
public class FallbackMountedStorage extends SimpleMountedStorage {
    public static final MapCodec<FallbackMountedStorage> CODEC = SimpleMountedStorage.codec(FallbackMountedStorage::new);

    public FallbackMountedStorage(Inventory handler) {
        super(AllMountedStorageTypes.FALLBACK, handler);
    }

    @Override
    protected Optional<Inventory> validate(Inventory handler) {
        return super.validate(handler).filter(FallbackMountedStorage::isValid);
    }

    public static boolean isValid(Inventory handler) {
        return handler.getClass() == ItemStackHandler.class;
    }
}
