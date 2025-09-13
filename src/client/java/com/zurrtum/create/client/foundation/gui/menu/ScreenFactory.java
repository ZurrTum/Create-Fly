package com.zurrtum.create.client.foundation.gui.menu;

import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@FunctionalInterface
public interface ScreenFactory<T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>, H> {
    U create(MinecraftClient mc, MenuType<H> type, int syncId, PlayerInventory playerInventory, Text title, RegistryByteBuf extraData);
}
