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

public record LinkedControllerStopLecternPacket(BlockPos lecternPos) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, LinkedControllerStopLecternPacket> CODEC = BlockPos.STREAM_CODEC.map(
        LinkedControllerStopLecternPacket::new,
        LinkedControllerStopLecternPacket::lecternPos
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onLinkedControllerStopLectern((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<LinkedControllerStopLecternPacket> type() {
        return AllPackets.LINKED_CONTROLLER_USE_LECTERN;
    }
}
