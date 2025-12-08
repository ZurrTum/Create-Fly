package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ElevatorTargetFloorPacket(int entityId, int targetY) implements Packet<ServerGamePacketListener> {
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
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onElevatorTargetFloor((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ElevatorTargetFloorPacket> type() {
        return AllPackets.ELEVATOR_SET_FLOOR;
    }
}
