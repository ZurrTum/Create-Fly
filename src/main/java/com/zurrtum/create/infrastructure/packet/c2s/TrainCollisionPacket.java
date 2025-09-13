package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record TrainCollisionPacket(int damage, int contraptionEntityId) implements C2SPacket {
    public static final PacketCodec<ByteBuf, TrainCollisionPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        TrainCollisionPacket::damage,
        PacketCodecs.INTEGER,
        TrainCollisionPacket::contraptionEntityId,
        TrainCollisionPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<TrainCollisionPacket> getPacketType() {
        return AllPackets.TRAIN_COLLISION;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, TrainCollisionPacket> callback() {
        return AllHandle::onTrainCollision;
    }
}
