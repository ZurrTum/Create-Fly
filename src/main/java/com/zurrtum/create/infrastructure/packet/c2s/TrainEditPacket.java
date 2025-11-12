package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrainEditPacket(UUID id, String name, ResourceLocation iconType, int mapColor) implements C2SPacket {
    public static StreamCodec<RegistryFriendlyByteBuf, TrainEditPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainEditPacket::id,
        ByteBufCodecs.stringUtf8(256),
        TrainEditPacket::name,
        ResourceLocation.STREAM_CODEC,
        TrainEditPacket::iconType,
        ByteBufCodecs.INT,
        TrainEditPacket::mapColor,
        TrainEditPacket::new
    );

    @Override
    public PacketType<TrainEditPacket> type() {
        return AllPackets.C_CONFIGURE_TRAIN;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, TrainEditPacket> callback() {
        return AllHandle::onTrainEdit;
    }
}
