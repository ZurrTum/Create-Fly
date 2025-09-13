package com.zurrtum.create.content.contraptions.behaviour.dispenser.storage;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.menu.MountedStorageMenus;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DispenserMountedStorage extends SimpleMountedStorage {
    public static final MapCodec<DispenserMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

    protected DispenserMountedStorage(MountedItemStorageType<?> type, Inventory handler) {
        super(type, handler);
    }

    public DispenserMountedStorage(Inventory handler) {
        this(AllMountedStorageTypes.DISPENSER, handler);
    }

    @Override
    @Nullable
    protected NamedScreenHandlerFactory createMenuProvider(
        Text name,
        Inventory handler,
        Predicate<PlayerEntity> stillValid,
        Consumer<PlayerEntity> onClose
    ) {
        return MountedStorageMenus.createGeneric9x9(name, handler, stillValid, onClose);
    }

    @Override
    protected void playOpeningSound(ServerWorld level, Vec3d pos) {
        // dispensers are silent
    }
}
