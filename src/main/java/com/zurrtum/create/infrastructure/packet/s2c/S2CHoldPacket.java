package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public record S2CHoldPacket<T extends ClientPlayPacketListener>(
    PacketType<Packet<ClientPlayPacketListener>> id, Consumer<AllClientHandle> callback
) implements Packet<ClientPlayPacketListener> {
    public S2CHoldPacket(String id, Consumer<AllClientHandle> callback) {
        this(new PacketType<>(NetworkSide.CLIENTBOUND, Identifier.of(MOD_ID, id)), callback);
    }

    public PacketCodec<RegistryByteBuf, Packet<ClientPlayPacketListener>> codec() {
        return PacketCodec.unit(this);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        callback.accept(AllClientHandle.INSTANCE);
    }

    @Override
    public PacketType<Packet<ClientPlayPacketListener>> getPacketType() {
        return id();
    }
}
