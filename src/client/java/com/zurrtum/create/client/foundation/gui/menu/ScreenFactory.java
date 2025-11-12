package com.zurrtum.create.client.foundation.gui.menu;

import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface ScreenFactory<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>, H> {
    U create(Minecraft mc, MenuType<H> type, int syncId, Inventory playerInventory, Component title, RegistryFriendlyByteBuf extraData);
}
