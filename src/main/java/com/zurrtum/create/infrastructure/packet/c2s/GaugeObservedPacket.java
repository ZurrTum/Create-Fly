package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record GaugeObservedPacket(BlockPos pos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, GaugeObservedPacket> CODEC = BlockPos.STREAM_CODEC.map(
        GaugeObservedPacket::new,
        GaugeObservedPacket::pos
    );

    @Override
    public PacketType<GaugeObservedPacket> type() {
        return AllPackets.OBSERVER_STRESSOMETER;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, GaugeObservedPacket> callback() {
        return AllHandle::onGaugeObserved;
    }
}
