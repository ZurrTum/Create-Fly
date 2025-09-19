package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ContraptionSeatMappingPacket(int entityId, Map<UUID, Integer> mapping, int dismountedId) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ContraptionSeatMappingPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ContraptionSeatMappingPacket::entityId,
        PacketCodecs.map(HashMap::new, Uuids.PACKET_CODEC, PacketCodecs.INTEGER),
        p -> new HashMap<>(p.mapping),
        PacketCodecs.INTEGER,
        ContraptionSeatMappingPacket::dismountedId,
        ContraptionSeatMappingPacket::new
    );

    public ContraptionSeatMappingPacket(int entityID, Map<UUID, Integer> mapping) {
        this(entityID, mapping, -1);
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionSeatMappingPacket> callback() {
        return AllClientHandle::onContraptionSeatMapping;
    }

    @Override
    public PacketType<ContraptionSeatMappingPacket> getPacketType() {
        return AllPackets.CONTRAPTION_SEAT_MAPPING;
    }
}
