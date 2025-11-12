package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record LogisticalStockRequestPacket(BlockPos pos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, LogisticalStockRequestPacket> CODEC = BlockPos.STREAM_CODEC.map(
        LogisticalStockRequestPacket::new,
        LogisticalStockRequestPacket::pos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LogisticalStockRequestPacket> type() {
        return AllPackets.LOGISTICS_STOCK_REQUEST;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, LogisticalStockRequestPacket> callback() {
        return AllHandle::onLogisticalStockRequest;
    }
}
