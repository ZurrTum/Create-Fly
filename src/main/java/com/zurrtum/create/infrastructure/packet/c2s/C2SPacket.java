package com.zurrtum.create.infrastructure.packet.c2s;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public interface C2SPacket extends Packet<ServerPlayPacketListener> {
    @Override
    default void apply(ServerPlayPacketListener listener) {
        if (runInMain()) {
            NetworkThreadUtils.forceMainThread(this, listener, ((ServerPlayNetworkHandler) listener).player.getEntityWorld());
        }
        callback().accept((ServerPlayNetworkHandler) listener, cast());
    }

    BiConsumer<ServerPlayNetworkHandler, ? extends C2SPacket> callback();

    default boolean runInMain() {
        return false;
    }

    @SuppressWarnings("unchecked")
    default <T extends C2SPacket> T cast() {
        return (T) this;
    }
}
