package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record LogisticalStockRequestPacket(BlockPos pos) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, LogisticalStockRequestPacket> CODEC = BlockPos.STREAM_CODEC.map(
        LogisticalStockRequestPacket::new,
        LogisticalStockRequestPacket::pos
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onLogisticalStockRequest((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<LogisticalStockRequestPacket> type() {
        return AllPackets.LOGISTICS_STOCK_REQUEST;
    }
}
