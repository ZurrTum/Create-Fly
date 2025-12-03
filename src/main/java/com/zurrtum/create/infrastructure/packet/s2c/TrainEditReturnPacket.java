package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record TrainEditReturnPacket(UUID id, String name, Identifier iconType, int mapColor) implements Packet<ClientPlayPacketListener> {
    public static PacketCodec<RegistryByteBuf, TrainEditReturnPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainEditReturnPacket::id,
        PacketCodecs.string(256),
        TrainEditReturnPacket::name,
        Identifier.PACKET_CODEC,
        TrainEditReturnPacket::iconType,
        PacketCodecs.INTEGER,
        TrainEditReturnPacket::mapColor,
        TrainEditReturnPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onTrainEditReturn(this);
    }

    @Override
    public PacketType<TrainEditReturnPacket> getPacketType() {
        return AllPackets.S_CONFIGURE_TRAIN;
    }
}
