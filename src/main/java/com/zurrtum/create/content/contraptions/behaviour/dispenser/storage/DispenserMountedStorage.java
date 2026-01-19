package com.zurrtum.create.content.contraptions.behaviour.dispenser.storage;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.menu.MountedStorageMenus;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DispenserMountedStorage extends SimpleMountedStorage {
    public static final MapCodec<DispenserMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

    protected DispenserMountedStorage(MountedItemStorageType<?> type, Container handler) {
        super(type, handler);
    }

    public DispenserMountedStorage(Container handler) {
        this(AllMountedStorageTypes.DISPENSER, handler);
    }

    @Override
    @Nullable
    protected MenuProvider createMenuProvider(Component name, Container handler, Predicate<Player> stillValid, Consumer<ContainerUser> onClose) {
        return MountedStorageMenus.createGeneric9x9(name, handler, stillValid, onClose);
    }

    @Override
    protected void playOpeningSound(ServerLevel level, Vec3 pos) {
        // dispensers are silent
    }
}
