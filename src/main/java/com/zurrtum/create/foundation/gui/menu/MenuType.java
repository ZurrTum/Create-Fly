package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface MenuType<H> {
    MenuBase<H> create(int syncId, Inventory playerInventory, H holder);

    @SuppressWarnings("unchecked")
    @Nullable
    default <T extends MenuBase<H>, S> S create(
        TriFunction<T, Inventory, Component, S> factory,
        int syncId,
        Inventory playerInventory,
        Component name,
        @Nullable H holder
    ) {
        if (holder == null) {
            return null;
        }
        return factory.apply((T) create(syncId, playerInventory, holder), playerInventory, name);
    }
}
