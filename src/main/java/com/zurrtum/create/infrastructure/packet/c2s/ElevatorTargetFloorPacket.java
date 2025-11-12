package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ElevatorTargetFloorPacket(int entityId, int targetY) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ElevatorTargetFloorPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ElevatorTargetFloorPacket::entityId,
        ByteBufCodecs.INT,
        ElevatorTargetFloorPacket::targetY,
        ElevatorTargetFloorPacket::new
    );

    public ElevatorTargetFloorPacket(AbstractContraptionEntity entity, int targetY) {
        this(entity.getId(), targetY);
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ElevatorTargetFloorPacket> type() {
        return AllPackets.ELEVATOR_SET_FLOOR;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ElevatorTargetFloorPacket> callback() {
        return AllHandle::onElevatorTargetFloor;
    }
}
