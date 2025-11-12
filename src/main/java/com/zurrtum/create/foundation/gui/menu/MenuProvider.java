package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.infrastructure.packet.s2c.OpenScreenPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface MenuProvider {
    default Component getDisplayName() {
        return Component.empty();
    }

    @Nullable MenuBase<?> createMenu(int syncId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData);

    default void openHandledScreen(ServerPlayer player) {
        openHandledScreen(player, this);
    }

    static void openHandledScreen(ServerPlayer player, MenuProvider provider) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        player.nextContainerCounter();

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        MenuBase<?> menu = provider.createMenu(player.containerCounter, player.getInventory(), player, buf);
        if (menu == null) {
            if (player.isSpectator()) {
                player.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
            }

            buf.release();
        } else {
            buf.readerIndex(0);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();
            player.connection.send(new OpenScreenPacket(menu.containerId, menu.getMenuType(), provider.getDisplayName(), data));
            player.initMenu(menu);
            player.containerMenu = menu;
        }
    }
}
