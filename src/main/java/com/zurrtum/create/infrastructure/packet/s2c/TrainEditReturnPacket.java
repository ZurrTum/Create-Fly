package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public record TrainEditReturnPacket(UUID id, String name, Identifier iconType, int mapColor) implements S2CPacket {
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
    public PacketType<TrainEditReturnPacket> type() {
        return AllPackets.S_CONFIGURE_TRAIN;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainEditReturnPacket> callback() {
        return AllClientHandle::onTrainEditReturn;
    }
}
