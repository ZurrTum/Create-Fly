package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record CurvedTrackSelectionPacket(BlockPos pos, BlockPos targetPos, boolean front, int segment, int slot) implements C2SPacket {
    public static final StreamCodec<ByteBuf, CurvedTrackSelectionPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        CurvedTrackSelectionPacket::pos,
        BlockPos.STREAM_CODEC,
        CurvedTrackSelectionPacket::targetPos,
        ByteBufCodecs.BOOL,
        CurvedTrackSelectionPacket::front,
        ByteBufCodecs.VAR_INT,
        CurvedTrackSelectionPacket::segment,
        ByteBufCodecs.VAR_INT,
        CurvedTrackSelectionPacket::slot,
        CurvedTrackSelectionPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<CurvedTrackSelectionPacket> type() {
        return AllPackets.SELECT_CURVED_TRACK;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, CurvedTrackSelectionPacket> callback() {
        return AllHandle::onCurvedTrackSelection;
    }
}
