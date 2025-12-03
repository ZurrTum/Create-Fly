package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RemoveTrainPacket(UUID id) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RemoveTrainPacket> CODEC = Uuids.PACKET_CODEC.xmap(RemoveTrainPacket::new, RemoveTrainPacket::id);

    public RemoveTrainPacket(Train train) {
        this(train.id);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onRemoveTrain(this);
    }

    @Override
    public PacketType<RemoveTrainPacket> getPacketType() {
        return AllPackets.REMOVE_TRAIN;
    }
}
