package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record LinkedControllerStopLecternPacket(BlockPos lecternPos) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, LinkedControllerStopLecternPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        LinkedControllerStopLecternPacket::new,
        LinkedControllerStopLecternPacket::lecternPos
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onLinkedControllerStopLectern((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<LinkedControllerStopLecternPacket> getPacketType() {
        return AllPackets.LINKED_CONTROLLER_USE_LECTERN;
    }
}
