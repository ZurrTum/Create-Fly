package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record ContraptionSeatMappingPacket(int entityId, Map<UUID, Integer> mapping, int dismountedId) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ContraptionSeatMappingPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ContraptionSeatMappingPacket::entityId,
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.INT),
        p -> new HashMap<>(p.mapping),
        ByteBufCodecs.INT,
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
    public PacketType<ContraptionSeatMappingPacket> type() {
        return AllPackets.CONTRAPTION_SEAT_MAPPING;
    }
}
