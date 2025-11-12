package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.apache.logging.log4j.util.TriConsumer;

public interface S2CPacket extends Packet<ClientGamePacketListener> {
    @Override
    default void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.call(callback(), listener, this);
    }

    default boolean runInMain() {
        return false;
    }

    <T> TriConsumer<AllClientHandle<T>, T, ? extends S2CPacket> callback();
}
