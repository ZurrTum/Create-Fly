package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record EjectorTriggerPacket(BlockPos pos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, EjectorTriggerPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        EjectorTriggerPacket::new,
        EjectorTriggerPacket::pos
    );

    @Override
    public PacketType<EjectorTriggerPacket> getPacketType() {
        return AllPackets.TRIGGER_EJECTOR;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, EjectorTriggerPacket> callback() {
        return AllHandle::onEjectorTrigger;
    }
}
