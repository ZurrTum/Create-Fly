package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record GantryContraptionUpdatePacket(
    int entityID, double coord, double motion, double sequenceLimit
) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, GantryContraptionUpdatePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        GantryContraptionUpdatePacket::entityID,
        PacketCodecs.DOUBLE,
        GantryContraptionUpdatePacket::coord,
        PacketCodecs.DOUBLE,
        GantryContraptionUpdatePacket::motion,
        PacketCodecs.DOUBLE,
        GantryContraptionUpdatePacket::sequenceLimit,
        GantryContraptionUpdatePacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onGantryContraptionUpdate(this);
    }

    @Override
    public PacketType<GantryContraptionUpdatePacket> getPacketType() {
        return AllPackets.GANTRY_UPDATE;
    }
}
