package com.zurrtum.create.infrastructure.packet.c2s;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public record C2SHoldPacket(
    PacketType<Packet<ServerPlayPacketListener>> id, Consumer<ServerPlayNetworkHandler> consumer
) implements Packet<ServerPlayPacketListener> {
    public C2SHoldPacket(String id, Consumer<ServerPlayNetworkHandler> callback) {
        this(new PacketType<>(NetworkSide.SERVERBOUND, Identifier.of(MOD_ID, id)), callback);
    }

    public PacketCodec<RegistryByteBuf, Packet<ServerPlayPacketListener>> codec() {
        return PacketCodec.unit(this);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        consumer.accept((ServerPlayNetworkHandler) listener);
    }

    @Override
    public PacketType<Packet<ServerPlayPacketListener>> getPacketType() {
        return id;
    }
}
