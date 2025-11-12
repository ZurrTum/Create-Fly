package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record HonkReturnPacket(UUID trainId, boolean isHonk) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, HonkReturnPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        HonkReturnPacket::trainId,
        ByteBufCodecs.BOOL,
        HonkReturnPacket::isHonk,
        HonkReturnPacket::new
    );

    public HonkReturnPacket(Train train, boolean isHonk) {
        this(train.id, isHonk);
    }

    @Override
    public PacketType<HonkReturnPacket> type() {
        return AllPackets.S_TRAIN_HONK;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, HonkReturnPacket> callback() {
        return AllClientHandle::onTrainHonkReturn;
    }
}
