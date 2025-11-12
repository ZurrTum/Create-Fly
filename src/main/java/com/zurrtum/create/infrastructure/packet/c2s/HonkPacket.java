package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;

import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record HonkPacket(UUID trainId, boolean isHonk) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, HonkPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        HonkPacket::trainId,
        ByteBufCodecs.BOOL,
        HonkPacket::isHonk,
        HonkPacket::new
    );

    public HonkPacket(Train train, boolean isHonk) {
        this(train.id, isHonk);
    }

    @Override
    public PacketType<HonkPacket> type() {
        return AllPackets.C_TRAIN_HONK;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, HonkPacket> callback() {
        return AllHandle::onTrainHonk;
    }
}
