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
import net.minecraft.util.math.BlockPos;

public record CurvedTrackDestroyPacket(
    BlockPos pos, BlockPos targetPos, BlockPos soundSource, boolean wrench
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, CurvedTrackDestroyPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        CurvedTrackDestroyPacket::pos,
        BlockPos.PACKET_CODEC,
        CurvedTrackDestroyPacket::targetPos,
        BlockPos.PACKET_CODEC,
        CurvedTrackDestroyPacket::soundSource,
        PacketCodecs.BOOLEAN,
        CurvedTrackDestroyPacket::wrench,
        CurvedTrackDestroyPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onCurvedTrackDestroy((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<CurvedTrackDestroyPacket> getPacketType() {
        return AllPackets.DESTROY_CURVED_TRACK;
    }
}
