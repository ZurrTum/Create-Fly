package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record SoulPulseEffectPacket(BlockPos pos, int distance, boolean canOverlap) implements S2CPacket {
    public static final StreamCodec<ByteBuf, SoulPulseEffectPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SoulPulseEffectPacket::pos,
        ByteBufCodecs.INT,
        SoulPulseEffectPacket::distance,
        ByteBufCodecs.BOOL,
        SoulPulseEffectPacket::canOverlap,
        SoulPulseEffectPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, SoulPulseEffectPacket> callback() {
        return AllClientHandle::onSoulPulseEffect;
    }

    @Override
    public PacketType<SoulPulseEffectPacket> type() {
        return AllPackets.SOUL_PULSE;
    }
}
