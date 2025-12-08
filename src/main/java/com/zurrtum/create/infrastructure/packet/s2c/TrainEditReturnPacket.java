package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record TrainEditReturnPacket(UUID id, String name, Identifier iconType, int mapColor) implements Packet<ClientGamePacketListener> {
    public static StreamCodec<RegistryFriendlyByteBuf, TrainEditReturnPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainEditReturnPacket::id,
        ByteBufCodecs.stringUtf8(256),
        TrainEditReturnPacket::name,
        Identifier.STREAM_CODEC,
        TrainEditReturnPacket::iconType,
        ByteBufCodecs.INT,
        TrainEditReturnPacket::mapColor,
        TrainEditReturnPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onTrainEditReturn(this);
    }

    @Override
    public PacketType<TrainEditReturnPacket> type() {
        return AllPackets.S_CONFIGURE_TRAIN;
    }
}
