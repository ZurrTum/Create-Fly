package com.zurrtum.create.api.contraption.storage.item.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Methods for creating generic menus usable by mounted storages.
 */
public class MountedStorageMenus {
    public static final List<ScreenHandlerType<?>> GENERIC_CHEST_MENUS = List.of(
        ScreenHandlerType.GENERIC_9X1,
        ScreenHandlerType.GENERIC_9X2,
        ScreenHandlerType.GENERIC_9X3,
        ScreenHandlerType.GENERIC_9X4,
        ScreenHandlerType.GENERIC_9X5,
        ScreenHandlerType.GENERIC_9X6
    );

    @Nullable
    public static NamedScreenHandlerFactory createGeneric(
        Text menuName,
        Inventory handler,
        Predicate<PlayerEntity> stillValid,
        Consumer<PlayerEntity> onClose
    ) {
        int size = handler.size();
        int rows = size / 9;
        if (rows < 1 || rows > 6)
            return null;

        // make sure rows are full
        if (size % 9 != 0)
            return null;

        ScreenHandlerType<?> type = GENERIC_CHEST_MENUS.get(rows - 1);
        Inventory wrapper = new StorageInteractionWrapper(handler, stillValid, onClose);
        ScreenHandlerFactory constructor = (id, inv, player) -> new GenericContainerScreenHandler(type, id, inv, wrapper, rows);
        return new SimpleNamedScreenHandlerFactory(constructor, menuName);
    }

    @Nullable
    public static NamedScreenHandlerFactory createGeneric9x9(
        Text name,
        Inventory handler,
        Predicate<PlayerEntity> stillValid,
        Consumer<PlayerEntity> onClose
    ) {
        if (handler.size() != 9)
            return null;

        Inventory wrapper = new StorageInteractionWrapper(handler, stillValid, onClose);
        ScreenHandlerFactory constructor = (id, inv, player) -> new Generic3x3ContainerScreenHandler(id, inv, wrapper);
        return new SimpleNamedScreenHandlerFactory(constructor, name);
    }
}
