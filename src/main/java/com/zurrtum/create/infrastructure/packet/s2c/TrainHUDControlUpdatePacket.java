package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

public record TrainHUDControlUpdatePacket(UUID trainId, Double throttle, double speed, int fuelTicks) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, TrainHUDControlUpdatePacket> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC,
        TrainHUDControlUpdatePacket::trainId,
        CatnipStreamCodecBuilders.nullable(PacketCodecs.DOUBLE),
        TrainHUDControlUpdatePacket::throttle,
        PacketCodecs.DOUBLE,
        TrainHUDControlUpdatePacket::speed,
        PacketCodecs.VAR_INT,
        TrainHUDControlUpdatePacket::fuelTicks,
        TrainHUDControlUpdatePacket::new
    );

    public TrainHUDControlUpdatePacket(Train train) {
        this(train.id, train.throttle, nonStalledSpeed(train), train.fuelTicks);
    }

    private static double nonStalledSpeed(Train train) {
        return train.speedBeforeStall == null ? train.speed : train.speedBeforeStall;
    }

    @Override
    public PacketType<TrainHUDControlUpdatePacket> getPacketType() {
        return AllPackets.S_TRAIN_HUD;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainHUDControlUpdatePacket> callback() {
        return AllClientHandle::onTrainHUDControlUpdate;
    }
}
