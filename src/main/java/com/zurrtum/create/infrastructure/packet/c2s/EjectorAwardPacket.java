package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record EjectorAwardPacket(BlockPos pos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, EjectorAwardPacket> CODEC = BlockPos.PACKET_CODEC.xmap(EjectorAwardPacket::new, EjectorAwardPacket::pos);

    @Override
    public PacketType<EjectorAwardPacket> getPacketType() {
        return AllPackets.EJECTOR_AWARD;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, EjectorAwardPacket> callback() {
        return AllHandle::onEjectorAward;
    }
}
