package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ContraptionStallPacket(int entityId, double x, double y, double z, float angle) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ContraptionStallPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ContraptionStallPacket::entityId,
        ByteBufCodecs.DOUBLE,
        ContraptionStallPacket::x,
        ByteBufCodecs.DOUBLE,
        ContraptionStallPacket::y,
        ByteBufCodecs.DOUBLE,
        ContraptionStallPacket::z,
        ByteBufCodecs.FLOAT,
        ContraptionStallPacket::angle,
        ContraptionStallPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionStall(this);
    }

    @Override
    public PacketType<ContraptionStallPacket> type() {
        return AllPackets.CONTRAPTION_STALL;
    }
}
