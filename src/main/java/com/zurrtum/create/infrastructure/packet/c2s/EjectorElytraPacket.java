package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record EjectorElytraPacket(BlockPos pos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, EjectorElytraPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        EjectorElytraPacket::new,
        EjectorElytraPacket::pos
    );

    @Override
    public PacketType<EjectorElytraPacket> getPacketType() {
        return AllPackets.EJECTOR_ELYTRA;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, EjectorElytraPacket> callback() {
        return AllHandle::onEjectorElytra;
    }
}
