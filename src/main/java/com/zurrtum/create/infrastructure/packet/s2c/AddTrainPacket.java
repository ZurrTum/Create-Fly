package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record AddTrainPacket(Train train) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, AddTrainPacket> CODEC = Train.STREAM_CODEC.xmap(AddTrainPacket::new, AddTrainPacket::train);

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, AddTrainPacket> callback() {
        return AllClientHandle::onAddTrain;
    }

    @Override
    public PacketType<AddTrainPacket> getPacketType() {
        return AllPackets.ADD_TRAIN;
    }
}
