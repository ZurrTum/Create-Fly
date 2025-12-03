package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record AddTrainPacket(Train train) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, AddTrainPacket> CODEC = Train.STREAM_CODEC.xmap(AddTrainPacket::new, AddTrainPacket::train);

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onAddTrain(this);
    }

    @Override
    public PacketType<AddTrainPacket> getPacketType() {
        return AllPackets.ADD_TRAIN;
    }
}
