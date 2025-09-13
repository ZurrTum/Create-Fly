package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.apache.logging.log4j.util.TriConsumer;

public interface S2CPacket extends Packet<ClientPlayPacketListener> {
    @Override
    default void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.call(callback(), listener, this);
    }

    default boolean runInMain() {
        return false;
    }

    <T> TriConsumer<AllClientHandle<T>, T, ? extends S2CPacket> callback();
}
