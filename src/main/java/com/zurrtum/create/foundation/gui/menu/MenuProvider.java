package com.zurrtum.create.foundation.gui.menu;

import com.zurrtum.create.infrastructure.packet.s2c.OpenScreenPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public interface MenuProvider {
    default Text getDisplayName() {
        return Text.empty();
    }

    @Nullable MenuBase<?> createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player, RegistryByteBuf extraData);

    default void openHandledScreen(ServerPlayerEntity player) {
        openHandledScreen(player, this);
    }

    static void openHandledScreen(ServerPlayerEntity player, MenuProvider provider) {
        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.closeHandledScreen();
        }

        player.incrementScreenHandlerSyncId();

        RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
        MenuBase<?> menu = provider.createMenu(player.screenHandlerSyncId, player.getInventory(), player, buf);
        if (menu == null) {
            if (player.isSpectator()) {
                player.sendMessage(Text.translatable("container.spectatorCantOpen").formatted(Formatting.RED), true);
            }

            buf.release();
        } else {
            buf.readerIndex(0);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();
            player.networkHandler.sendPacket(new OpenScreenPacket(menu.syncId, menu.getMenuType(), provider.getDisplayName(), data));
            player.onScreenHandlerOpened(menu);
            player.currentScreenHandler = menu;
        }
    }
}
