package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record FunnelFlapPacket(BlockPos pos, boolean inwards) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, FunnelFlapPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        FunnelFlapPacket::pos,
        PacketCodecs.BOOLEAN,
        FunnelFlapPacket::inwards,
        FunnelFlapPacket::new
    );

    public FunnelFlapPacket(FunnelBlockEntity blockEntity, boolean inwards) {
        this(blockEntity.getPos(), inwards);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onFunnelFlap(listener, this);
    }

    @Override
    public PacketType<FunnelFlapPacket> getPacketType() {
        return AllPackets.FUNNEL_FLAP;
    }
}
