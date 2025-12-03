package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record TrackGraphRequestPacket(int netId) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, TrackGraphRequestPacket> CODEC = PacketCodecs.INTEGER.xmap(
        TrackGraphRequestPacket::new,
        TrackGraphRequestPacket::netId
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onTrackGraphRequest((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<TrackGraphRequestPacket> getPacketType() {
        return AllPackets.TRACK_GRAPH_REQUEST;
    }
}
