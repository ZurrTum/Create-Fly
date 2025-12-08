package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrainCollisionPacket(int damage, int contraptionEntityId) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, TrainCollisionPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        TrainCollisionPacket::damage,
        ByteBufCodecs.INT,
        TrainCollisionPacket::contraptionEntityId,
        TrainCollisionPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onTrainCollision((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<TrainCollisionPacket> type() {
        return AllPackets.TRAIN_COLLISION;
    }
}
