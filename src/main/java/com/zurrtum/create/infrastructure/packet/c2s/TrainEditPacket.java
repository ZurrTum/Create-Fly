package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record TrainEditPacket(UUID id, String name, Identifier iconType, int mapColor) implements Packet<ServerPlayPacketListener> {
    public static PacketCodec<RegistryByteBuf, TrainEditPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainEditPacket::id,
        PacketCodecs.string(256),
        TrainEditPacket::name,
        Identifier.PACKET_CODEC,
        TrainEditPacket::iconType,
        PacketCodecs.INTEGER,
        TrainEditPacket::mapColor,
        TrainEditPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onTrainEdit((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<TrainEditPacket> getPacketType() {
        return AllPackets.C_CONFIGURE_TRAIN;
    }
}
