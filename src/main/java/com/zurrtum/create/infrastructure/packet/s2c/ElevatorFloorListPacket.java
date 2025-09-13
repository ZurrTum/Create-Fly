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
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public record ElevatorFloorListPacket(int entityId, List<IntAttached<Couple<String>>> floors) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ElevatorFloorListPacket> getPacketType() {
        return AllPackets.UPDATE_ELEVATOR_FLOORS;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ElevatorFloorListPacket> callback() {
        return AllClientHandle::onElevatorFloorList;
    }
}
