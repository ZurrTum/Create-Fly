package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record AddTrainPacket(Train train) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, AddTrainPacket> CODEC = Train.STREAM_CODEC.map(
        AddTrainPacket::new,
        AddTrainPacket::train
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, AddTrainPacket> callback() {
        return AllClientHandle::onAddTrain;
    }

    @Override
    public PacketType<AddTrainPacket> type() {
        return AllPackets.ADD_TRAIN;
    }
}
