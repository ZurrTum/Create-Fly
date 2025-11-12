package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrackGraphRequestPacket(int netId) implements C2SPacket {
    public static final StreamCodec<ByteBuf, TrackGraphRequestPacket> CODEC = ByteBufCodecs.INT.map(
        TrackGraphRequestPacket::new,
        TrackGraphRequestPacket::netId
    );

    @Override
    public PacketType<TrackGraphRequestPacket> type() {
        return AllPackets.TRACK_GRAPH_REQUEST;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, TrackGraphRequestPacket> callback() {
        return AllHandle::onTrackGraphRequest;
    }
}
