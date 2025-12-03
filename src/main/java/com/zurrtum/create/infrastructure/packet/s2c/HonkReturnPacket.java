package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record HonkReturnPacket(UUID trainId, boolean isHonk) implements Packet<ClientPlayPacketListener> {
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
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onTrainHonkReturn(this);
    }

    @Override
    public PacketType<HonkReturnPacket> getPacketType() {
        return AllPackets.S_TRAIN_HONK;
    }
}
