package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

public record HonkReturnPacket(UUID trainId, boolean isHonk) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, HonkReturnPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        HonkReturnPacket::trainId,
        PacketCodecs.BOOLEAN,
        HonkReturnPacket::isHonk,
        HonkReturnPacket::new
    );

    public HonkReturnPacket(Train train, boolean isHonk) {
        this(train.id, isHonk);
    }

    @Override
    public PacketType<HonkReturnPacket> getPacketType() {
        return AllPackets.S_TRAIN_HONK;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, HonkReturnPacket> callback() {
        return AllClientHandle::onTrainHonkReturn;
    }
}
