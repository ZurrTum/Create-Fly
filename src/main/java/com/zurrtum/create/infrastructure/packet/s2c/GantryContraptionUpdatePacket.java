package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record GantryContraptionUpdatePacket(
    int entityID, double coord, double motion, double sequenceLimit
) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, GantryContraptionUpdatePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        GantryContraptionUpdatePacket::entityID,
        ByteBufCodecs.DOUBLE,
        GantryContraptionUpdatePacket::coord,
        ByteBufCodecs.DOUBLE,
        GantryContraptionUpdatePacket::motion,
        ByteBufCodecs.DOUBLE,
        GantryContraptionUpdatePacket::sequenceLimit,
        GantryContraptionUpdatePacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onGantryContraptionUpdate(this);
    }

    @Override
    public PacketType<GantryContraptionUpdatePacket> type() {
        return AllPackets.GANTRY_UPDATE;
    }
}
