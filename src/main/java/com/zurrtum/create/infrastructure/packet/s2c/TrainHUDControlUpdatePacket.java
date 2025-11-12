package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record TrainHUDControlUpdatePacket(UUID trainId, Double throttle, double speed, int fuelTicks) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainHUDControlUpdatePacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainHUDControlUpdatePacket::trainId,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.DOUBLE),
        TrainHUDControlUpdatePacket::throttle,
        ByteBufCodecs.DOUBLE,
        TrainHUDControlUpdatePacket::speed,
        ByteBufCodecs.VAR_INT,
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
    public PacketType<TrainHUDControlUpdatePacket> type() {
        return AllPackets.S_TRAIN_HUD;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainHUDControlUpdatePacket> callback() {
        return AllClientHandle::onTrainHUDControlUpdate;
    }
}
