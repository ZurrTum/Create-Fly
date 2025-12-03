package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record HonkPacket(UUID trainId, boolean isHonk) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, HonkPacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        HonkPacket::trainId,
        PacketCodecs.BOOLEAN,
        HonkPacket::isHonk,
        HonkPacket::new
    );

    public HonkPacket(Train train, boolean isHonk) {
        this(train.id, isHonk);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onTrainHonk((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<HonkPacket> getPacketType() {
        return AllPackets.C_TRAIN_HONK;
    }
}
