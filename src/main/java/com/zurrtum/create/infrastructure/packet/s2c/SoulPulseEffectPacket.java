package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record SoulPulseEffectPacket(BlockPos pos, int distance, boolean canOverlap) implements S2CPacket {
    public static final PacketCodec<ByteBuf, SoulPulseEffectPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        SoulPulseEffectPacket::pos,
        PacketCodecs.INTEGER,
        SoulPulseEffectPacket::distance,
        PacketCodecs.BOOLEAN,
        SoulPulseEffectPacket::canOverlap,
        SoulPulseEffectPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, SoulPulseEffectPacket> callback() {
        return AllClientHandle::onSoulPulseEffect;
    }

    @Override
    public PacketType<SoulPulseEffectPacket> getPacketType() {
        return AllPackets.SOUL_PULSE;
    }
}
