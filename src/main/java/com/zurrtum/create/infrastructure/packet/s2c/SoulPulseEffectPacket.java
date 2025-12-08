package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record SoulPulseEffectPacket(BlockPos pos, int distance, boolean canOverlap) implements Packet<ClientGamePacketListener> {
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
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onSoulPulseEffect(this);
    }

    @Override
    public PacketType<SoulPulseEffectPacket> type() {
        return AllPackets.SOUL_PULSE;
    }
}
