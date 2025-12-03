package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

import java.util.List;

public record ElevatorFloorListPacket(int entityId, List<IntAttached<Couple<String>>> floors) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ElevatorFloorListPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ElevatorFloorListPacket::entityId,
        CatnipStreamCodecBuilders.list(IntAttached.streamCodec(Couple.streamCodec(PacketCodecs.STRING))),
        ElevatorFloorListPacket::floors,
        ElevatorFloorListPacket::new
    );

    public ElevatorFloorListPacket(AbstractContraptionEntity entity, List<IntAttached<Couple<String>>> floors) {
        this(entity.getId(), floors);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onElevatorFloorList(listener, this);
    }

    @Override
    public PacketType<ElevatorFloorListPacket> getPacketType() {
        return AllPackets.UPDATE_ELEVATOR_FLOORS;
    }
}
