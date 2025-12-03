package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record ContraptionStallPacket(int entityId, double x, double y, double z, float angle) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ContraptionStallPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ContraptionStallPacket::entityId,
        PacketCodecs.DOUBLE,
        ContraptionStallPacket::x,
        PacketCodecs.DOUBLE,
        ContraptionStallPacket::y,
        PacketCodecs.DOUBLE,
        ContraptionStallPacket::z,
        PacketCodecs.FLOAT,
        ContraptionStallPacket::angle,
        ContraptionStallPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionStall(this);
    }

    @Override
    public PacketType<ContraptionStallPacket> getPacketType() {
        return AllPackets.CONTRAPTION_STALL;
    }
}
