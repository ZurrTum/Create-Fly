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

public record EjectorAwardPacket(BlockPos pos) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, EjectorAwardPacket> CODEC = BlockPos.STREAM_CODEC.map(EjectorAwardPacket::new, EjectorAwardPacket::pos);

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onEjectorAward((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<EjectorAwardPacket> type() {
        return AllPackets.EJECTOR_AWARD;
    }
}
