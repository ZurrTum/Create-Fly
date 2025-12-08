package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record AddTrainPacket(Train train) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, AddTrainPacket> CODEC = Train.STREAM_CODEC.map(
        AddTrainPacket::new,
        AddTrainPacket::train
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onAddTrain(this);
    }

    @Override
    public PacketType<AddTrainPacket> type() {
        return AllPackets.ADD_TRAIN;
    }
}
