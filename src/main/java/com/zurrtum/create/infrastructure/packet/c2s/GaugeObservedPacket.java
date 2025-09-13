package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record GaugeObservedPacket(BlockPos pos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, GaugeObservedPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        GaugeObservedPacket::new,
        GaugeObservedPacket::pos
    );

    @Override
    public PacketType<GaugeObservedPacket> getPacketType() {
        return AllPackets.OBSERVER_STRESSOMETER;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, GaugeObservedPacket> callback() {
        return AllHandle::onGaugeObserved;
    }
}
