package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record StockKeeperLockPacket(BlockPos pos, boolean lock) implements C2SPacket {
    public static final PacketCodec<ByteBuf, StockKeeperLockPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        StockKeeperLockPacket::pos,
        PacketCodecs.BOOLEAN,
        StockKeeperLockPacket::lock,
        StockKeeperLockPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<StockKeeperLockPacket> getPacketType() {
        return AllPackets.LOCK_STOCK_KEEPER;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, StockKeeperLockPacket> callback() {
        return AllHandle::onStockKeeperLock;
    }
}
