package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record EjectorElytraPacket(BlockPos pos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, EjectorElytraPacket> CODEC = BlockPos.STREAM_CODEC.map(
        EjectorElytraPacket::new,
        EjectorElytraPacket::pos
    );

    @Override
    public PacketType<EjectorElytraPacket> type() {
        return AllPackets.EJECTOR_ELYTRA;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, EjectorElytraPacket> callback() {
        return AllHandle::onEjectorElytra;
    }
}
