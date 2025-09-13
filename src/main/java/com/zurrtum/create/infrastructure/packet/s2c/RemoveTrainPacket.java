package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;

public record RemoveTrainPacket(UUID id) implements S2CPacket {
    public static final PacketCodec<ByteBuf, RemoveTrainPacket> CODEC = Uuids.PACKET_CODEC.xmap(RemoveTrainPacket::new, RemoveTrainPacket::id);

    public RemoveTrainPacket(Train train) {
        this(train.id);
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, RemoveTrainPacket> callback() {
        return AllClientHandle::onRemoveTrain;
    }

    @Override
    public PacketType<RemoveTrainPacket> getPacketType() {
        return AllPackets.REMOVE_TRAIN;
    }
}
