package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
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

public record HonkPacket(UUID trainId, boolean isHonk) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, HonkPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        HonkPacket::trainId,
        ByteBufCodecs.BOOL,
        HonkPacket::isHonk,
        HonkPacket::new
    );

    public HonkPacket(Train train, boolean isHonk) {
        this(train.id, isHonk);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onTrainHonk((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<HonkPacket> type() {
        return AllPackets.C_TRAIN_HONK;
    }
}
