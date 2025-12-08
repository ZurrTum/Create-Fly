package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.UUID;

public record RemoveTrainPacket(UUID id) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, RemoveTrainPacket> CODEC = UUIDUtil.STREAM_CODEC.map(RemoveTrainPacket::new, RemoveTrainPacket::id);

    public RemoveTrainPacket(Train train) {
        this(train.id);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onRemoveTrain(this);
    }

    @Override
    public PacketType<RemoveTrainPacket> type() {
        return AllPackets.REMOVE_TRAIN;
    }
}
