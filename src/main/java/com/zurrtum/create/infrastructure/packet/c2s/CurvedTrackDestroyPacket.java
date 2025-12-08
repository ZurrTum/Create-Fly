package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record CurvedTrackDestroyPacket(
    BlockPos pos, BlockPos targetPos, BlockPos soundSource, boolean wrench
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, CurvedTrackDestroyPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        CurvedTrackDestroyPacket::pos,
        BlockPos.STREAM_CODEC,
        CurvedTrackDestroyPacket::targetPos,
        BlockPos.STREAM_CODEC,
        CurvedTrackDestroyPacket::soundSource,
        ByteBufCodecs.BOOL,
        CurvedTrackDestroyPacket::wrench,
        CurvedTrackDestroyPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onCurvedTrackDestroy((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<CurvedTrackDestroyPacket> type() {
        return AllPackets.DESTROY_CURVED_TRACK;
    }
}
