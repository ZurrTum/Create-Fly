package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Uuids;

import java.util.UUID;
import java.util.function.BiConsumer;

public record TrainHUDUpdatePacket(UUID trainId, Double throttle) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, TrainHUDUpdatePacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainHUDUpdatePacket::trainId,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.DOUBLE),
        TrainHUDUpdatePacket::throttle,
        TrainHUDUpdatePacket::new
    );

    public TrainHUDUpdatePacket(Train train, Double sendThrottle) {
        this(train.id, sendThrottle);
    }

    @Override
    public PacketType<TrainHUDUpdatePacket> getPacketType() {
        return AllPackets.C_TRAIN_HUD;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, TrainHUDUpdatePacket> callback() {
        return AllHandle::onTrainHUDUpdate;
    }
}
