package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record TrainCollisionPacket(int damage, int contraptionEntityId) implements C2SPacket {
    public static final StreamCodec<ByteBuf, TrainCollisionPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        TrainCollisionPacket::damage,
        ByteBufCodecs.INT,
        TrainCollisionPacket::contraptionEntityId,
        TrainCollisionPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<TrainCollisionPacket> type() {
        return AllPackets.TRAIN_COLLISION;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, TrainCollisionPacket> callback() {
        return AllHandle::onTrainCollision;
    }
}
