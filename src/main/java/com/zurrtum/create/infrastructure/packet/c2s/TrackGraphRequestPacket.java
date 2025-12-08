package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrackGraphRequestPacket(int netId) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, TrackGraphRequestPacket> CODEC = ByteBufCodecs.INT.map(
        TrackGraphRequestPacket::new,
        TrackGraphRequestPacket::netId
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onTrackGraphRequest((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<TrackGraphRequestPacket> type() {
        return AllPackets.TRACK_GRAPH_REQUEST;
    }
}
