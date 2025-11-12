package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record RemoveTrainPacket(UUID id) implements S2CPacket {
    public static final StreamCodec<ByteBuf, RemoveTrainPacket> CODEC = UUIDUtil.STREAM_CODEC.map(RemoveTrainPacket::new, RemoveTrainPacket::id);

    public RemoveTrainPacket(Train train) {
        this(train.id);
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, RemoveTrainPacket> callback() {
        return AllClientHandle::onRemoveTrain;
    }

    @Override
    public PacketType<RemoveTrainPacket> type() {
        return AllPackets.REMOVE_TRAIN;
    }
}
