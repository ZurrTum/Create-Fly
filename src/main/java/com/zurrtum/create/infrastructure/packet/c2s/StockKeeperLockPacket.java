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

public record StockKeeperLockPacket(BlockPos pos, boolean lock) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, StockKeeperLockPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        StockKeeperLockPacket::pos,
        ByteBufCodecs.BOOL,
        StockKeeperLockPacket::lock,
        StockKeeperLockPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onStockKeeperLock((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<StockKeeperLockPacket> type() {
        return AllPackets.LOCK_STOCK_KEEPER;
    }
}
