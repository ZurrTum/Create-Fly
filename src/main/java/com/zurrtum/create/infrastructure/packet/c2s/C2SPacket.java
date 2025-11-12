package com.zurrtum.create.infrastructure.packet.c2s;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.function.BiConsumer;

public interface C2SPacket extends Packet<ServerGamePacketListener> {
    @Override
    default void handle(ServerGamePacketListener listener) {
        if (runInMain()) {
            PacketUtils.ensureRunningOnSameThread(this, listener, ((ServerGamePacketListenerImpl) listener).player.level());
        }
        callback().accept((ServerGamePacketListenerImpl) listener, cast());
    }

    BiConsumer<ServerGamePacketListenerImpl, ? extends C2SPacket> callback();

    default boolean runInMain() {
        return false;
    }

    @SuppressWarnings("unchecked")
    default <T extends C2SPacket> T cast() {
        return (T) this;
    }
}
