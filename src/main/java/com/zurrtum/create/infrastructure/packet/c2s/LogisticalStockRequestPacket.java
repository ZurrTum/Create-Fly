package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record LogisticalStockRequestPacket(BlockPos pos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, LogisticalStockRequestPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        LogisticalStockRequestPacket::new,
        LogisticalStockRequestPacket::pos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LogisticalStockRequestPacket> getPacketType() {
        return AllPackets.LOGISTICS_STOCK_REQUEST;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, LogisticalStockRequestPacket> callback() {
        return AllHandle::onLogisticalStockRequest;
    }
}
