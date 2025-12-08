package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

public record TrainHUDUpdatePacket(UUID trainId, Double throttle) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainHUDUpdatePacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        TrainHUDUpdatePacket::trainId,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.DOUBLE),
        TrainHUDUpdatePacket::throttle,
        TrainHUDUpdatePacket::new
    );

    public TrainHUDUpdatePacket(Train train, Double sendThrottle) {
        this(train.id, sendThrottle);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onTrainHUDUpdate((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<TrainHUDUpdatePacket> type() {
        return AllPackets.C_TRAIN_HUD;
    }
}
