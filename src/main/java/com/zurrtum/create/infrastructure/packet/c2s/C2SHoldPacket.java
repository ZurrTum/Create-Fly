package com.zurrtum.create.infrastructure.packet.c2s;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static com.zurrtum.create.Create.MOD_ID;

public record C2SHoldPacket(PacketType<Packet<ServerGamePacketListener>> id, Consumer<ServerGamePacketListenerImpl> consumer) implements C2SPacket {
    public C2SHoldPacket(String id, Consumer<ServerGamePacketListenerImpl> callback) {
        this(new PacketType<>(PacketFlow.SERVERBOUND, Identifier.fromNamespaceAndPath(MOD_ID, id)), callback);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Packet<ServerGamePacketListener>> codec() {
        return StreamCodec.unit(this);
    }

    @Override
    public PacketType<Packet<ServerGamePacketListener>> type() {
        return id;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, C2SHoldPacket> callback() {
        return (listener, packet) -> consumer.accept(listener);
    }
}
