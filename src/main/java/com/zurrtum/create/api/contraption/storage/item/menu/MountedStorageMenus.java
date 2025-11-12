package com.zurrtum.create.api.contraption.storage.item.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Methods for creating generic menus usable by mounted storages.
 */
public class MountedStorageMenus {
    public static final List<MenuType<?>> GENERIC_CHEST_MENUS = List.of(
        MenuType.GENERIC_9x1,
        MenuType.GENERIC_9x2,
        MenuType.GENERIC_9x3,
        MenuType.GENERIC_9x4,
        MenuType.GENERIC_9x5,
        MenuType.GENERIC_9x6
    );

    @Nullable
    public static MenuProvider createGeneric(Component menuName, Container handler, Predicate<Player> stillValid, Consumer<ContainerUser> onClose) {
        int size = handler.getContainerSize();
        int rows = size / 9;
        if (rows < 1 || rows > 6)
            return null;

        // make sure rows are full
        if (size % 9 != 0)
            return null;

        MenuType<?> type = GENERIC_CHEST_MENUS.get(rows - 1);
        Container wrapper = new StorageInteractionWrapper(handler, stillValid, onClose);
        MenuConstructor constructor = (id, inv, player) -> new ChestMenu(type, id, inv, wrapper, rows);
        return new SimpleMenuProvider(constructor, menuName);
    }

    @Nullable
    public static MenuProvider createGeneric9x9(Component name, Container handler, Predicate<Player> stillValid, Consumer<ContainerUser> onClose) {
        if (handler.getContainerSize() != 9)
            return null;

        Container wrapper = new StorageInteractionWrapper(handler, stillValid, onClose);
        MenuConstructor constructor = (id, inv, player) -> new DispenserMenu(id, inv, wrapper);
        return new SimpleMenuProvider(constructor, name);
    }
}
