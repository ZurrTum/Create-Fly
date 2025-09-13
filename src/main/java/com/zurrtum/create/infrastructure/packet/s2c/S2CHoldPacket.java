package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

import static com.zurrtum.create.Create.MOD_ID;

@SuppressWarnings("unchecked")
public record S2CHoldPacket<T extends ClientPlayPacketListener>(
    PacketType<Packet<ClientPlayPacketListener>> type, BiConsumer<AllClientHandle<T>, T> consumer
) implements S2CPacket {
    public S2CHoldPacket(String id, BiConsumer<AllClientHandle<T>, T> callback) {
        this(new PacketType<>(NetworkSide.CLIENTBOUND, Identifier.of(MOD_ID, id)), callback);
    }

    public PacketCodec<RegistryByteBuf, Packet<ClientPlayPacketListener>> codec() {
        return PacketCodec.unit(this);
    }

    @Override
    public PacketType<Packet<ClientPlayPacketListener>> getPacketType() {
        return type();
    }

    @Override
    public TriConsumer<AllClientHandle<T>, T, S2CHoldPacket<T>> callback() {
        return (instance, listener, packet) -> consumer.accept(instance, listener);
    }
}
