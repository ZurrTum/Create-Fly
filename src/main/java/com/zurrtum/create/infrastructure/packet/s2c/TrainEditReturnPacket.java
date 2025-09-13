package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

public record TrainEditReturnPacket(UUID id, String name, Identifier iconType, int mapColor) implements S2CPacket {
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
    public PacketType<TrainEditReturnPacket> getPacketType() {
        return AllPackets.S_CONFIGURE_TRAIN;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainEditReturnPacket> callback() {
        return AllClientHandle::onTrainEditReturn;
    }
}
