package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

public record TrainEditPacket(UUID id, String name, Identifier iconType, int mapColor) implements Packet<ServerGamePacketListener> {
    public static StreamCodec<RegistryFriendlyByteBuf, TrainEditPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainEditPacket::id,
        ByteBufCodecs.stringUtf8(256),
        TrainEditPacket::name,
        Identifier.STREAM_CODEC,
        TrainEditPacket::iconType,
        ByteBufCodecs.INT,
        TrainEditPacket::mapColor,
        TrainEditPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onTrainEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<TrainEditPacket> type() {
        return AllPackets.C_CONFIGURE_TRAIN;
    }
}
