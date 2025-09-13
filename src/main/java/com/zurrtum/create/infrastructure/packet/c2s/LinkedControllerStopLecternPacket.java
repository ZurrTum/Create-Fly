package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record LinkedControllerStopLecternPacket(BlockPos lecternPos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, LinkedControllerStopLecternPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        LinkedControllerStopLecternPacket::new,
        LinkedControllerStopLecternPacket::lecternPos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<LinkedControllerStopLecternPacket> getPacketType() {
        return AllPackets.LINKED_CONTROLLER_USE_LECTERN;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, LinkedControllerStopLecternPacket> callback() {
        return AllHandle::onLinkedControllerStopLectern;
    }
}
