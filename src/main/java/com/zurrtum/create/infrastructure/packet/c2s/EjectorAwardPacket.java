package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record EjectorAwardPacket(BlockPos pos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, EjectorAwardPacket> CODEC = BlockPos.STREAM_CODEC.map(EjectorAwardPacket::new, EjectorAwardPacket::pos);

    @Override
    public PacketType<EjectorAwardPacket> type() {
        return AllPackets.EJECTOR_AWARD;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, EjectorAwardPacket> callback() {
        return AllHandle::onEjectorAward;
    }
}
