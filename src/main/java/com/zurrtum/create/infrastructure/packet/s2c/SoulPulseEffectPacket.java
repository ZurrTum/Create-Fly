package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record SoulPulseEffectPacket(BlockPos pos, int distance, boolean canOverlap) implements Packet<ClientPlayPacketListener> {
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
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onSoulPulseEffect(this);
    }

    @Override
    public PacketType<SoulPulseEffectPacket> getPacketType() {
        return AllPackets.SOUL_PULSE;
    }
}
