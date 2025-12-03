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

public record CurvedTrackSelectionPacket(
    BlockPos pos, BlockPos targetPos, boolean front, int segment, int slot
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, CurvedTrackSelectionPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        CurvedTrackSelectionPacket::pos,
        BlockPos.PACKET_CODEC,
        CurvedTrackSelectionPacket::targetPos,
        PacketCodecs.BOOLEAN,
        CurvedTrackSelectionPacket::front,
        PacketCodecs.VAR_INT,
        CurvedTrackSelectionPacket::segment,
        PacketCodecs.VAR_INT,
        CurvedTrackSelectionPacket::slot,
        CurvedTrackSelectionPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onCurvedTrackSelection((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<CurvedTrackSelectionPacket> getPacketType() {
        return AllPackets.SELECT_CURVED_TRACK;
    }
}
