package com.zurrtum.create.foundation.gui.menu;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;

@FunctionalInterface
public interface MenuType<H> {
    MenuBase<H> create(int syncId, PlayerInventory playerInventory, H holder);

    @SuppressWarnings("unchecked")
    default <T extends MenuBase<H>, S> S create(
        TriFunction<T, PlayerInventory, Text, S> factory,
        int syncId,
        PlayerInventory playerInventory,
        Text name,
        H holder
    ) {
        if (holder == null) {
            return null;
        }
        return factory.apply((T) create(syncId, playerInventory, holder), playerInventory, name);
    }
}
