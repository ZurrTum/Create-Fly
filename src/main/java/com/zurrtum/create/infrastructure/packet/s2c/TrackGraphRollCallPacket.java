package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public record TrackGraphRollCallPacket(List<Entry> entries) implements S2CPacket {
    public static final PacketCodec<ByteBuf, TrackGraphRollCallPacket> CODEC = CatnipStreamCodecBuilders.list(Entry.STREAM_CODEC)
        .xmap(TrackGraphRollCallPacket::new, TrackGraphRollCallPacket::entries);

    public static TrackGraphRollCallPacket ofServer() {
        List<Entry> entries = new ArrayList<>();
        for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
            entries.add(new Entry(graph.netId, graph.getChecksum()));
        }
        return new TrackGraphRollCallPacket(entries);
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrackGraphRollCallPacket> callback() {
        return AllClientHandle::onTrackGraphRollCall;
    }

    @Override
    public PacketType<TrackGraphRollCallPacket> getPacketType() {
        return AllPackets.TRACK_GRAPH_ROLL_CALL;
    }

    public record Entry(int netId, int checksum) {
        public static final PacketCodec<ByteBuf, Entry> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            Entry::netId,
            PacketCodecs.INTEGER,
            Entry::checksum,
            Entry::new
        );
    }
}
