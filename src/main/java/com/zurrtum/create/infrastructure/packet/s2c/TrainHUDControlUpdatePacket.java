package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.UUID;

public record TrainHUDControlUpdatePacket(UUID trainId, Double throttle, double speed, int fuelTicks) implements Packet<ClientGamePacketListener> {
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
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onTrainHUDControlUpdate(this);
    }

    @Override
    public PacketType<TrainHUDControlUpdatePacket> type() {
        return AllPackets.S_TRAIN_HUD;
    }
}
