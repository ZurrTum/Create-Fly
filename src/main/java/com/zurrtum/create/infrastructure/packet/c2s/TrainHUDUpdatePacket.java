package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.entity.Train;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrainHUDUpdatePacket(UUID trainId, Double throttle) implements C2SPacket {
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
    public PacketType<TrainHUDUpdatePacket> type() {
        return AllPackets.C_TRAIN_HUD;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, TrainHUDUpdatePacket> callback() {
        return AllHandle::onTrainHUDUpdate;
    }
}
