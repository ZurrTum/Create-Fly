package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record TrackGraphRequestPacket(int netId) implements C2SPacket {
    public static final PacketCodec<ByteBuf, TrackGraphRequestPacket> CODEC = PacketCodecs.INTEGER.xmap(
        TrackGraphRequestPacket::new,
        TrackGraphRequestPacket::netId
    );

    @Override
    public PacketType<TrackGraphRequestPacket> getPacketType() {
        return AllPackets.TRACK_GRAPH_REQUEST;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, TrackGraphRequestPacket> callback() {
        return AllHandle::onTrackGraphRequest;
    }
}
