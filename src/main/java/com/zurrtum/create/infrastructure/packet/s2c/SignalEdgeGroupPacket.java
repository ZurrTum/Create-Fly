package com.zurrtum.create.infrastructure.packet.s2c;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.signal.EdgeGroupColor;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record SignalEdgeGroupPacket(List<UUID> ids, List<EdgeGroupColor> colors, boolean add) implements S2CPacket {
    public static final StreamCodec<ByteBuf, SignalEdgeGroupPacket> CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC),
        SignalEdgeGroupPacket::ids,
        CatnipStreamCodecBuilders.list(EdgeGroupColor.STREAM_CODEC),
        SignalEdgeGroupPacket::colors,
        ByteBufCodecs.BOOL,
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
    public PacketType<SignalEdgeGroupPacket> type() {
        return AllPackets.SYNC_EDGE_GROUP;
    }
}
