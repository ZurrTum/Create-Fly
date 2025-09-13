package com.zurrtum.create.infrastructure.packet.s2c;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.signal.EdgeGroupColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.UUID;

public record SignalEdgeGroupPacket(List<UUID> ids, List<EdgeGroupColor> colors, boolean add) implements S2CPacket {
    public static final PacketCodec<ByteBuf, SignalEdgeGroupPacket> CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.list(Uuids.PACKET_CODEC),
        SignalEdgeGroupPacket::ids,
        CatnipStreamCodecBuilders.list(EdgeGroupColor.STREAM_CODEC),
        SignalEdgeGroupPacket::colors,
        PacketCodecs.BOOLEAN,
        SignalEdgeGroupPacket::add,
        SignalEdgeGroupPacket::new
    );

    public SignalEdgeGroupPacket(UUID id, EdgeGroupColor color) {
        this(ImmutableList.of(id), ImmutableList.of(color), true);
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, SignalEdgeGroupPacket> callback() {
        return AllClientHandle::onSignalEdgeGroup;
    }

    @Override
    public PacketType<SignalEdgeGroupPacket> getPacketType() {
        return AllPackets.SYNC_EDGE_GROUP;
    }
}
