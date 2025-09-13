package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record ElevatorTargetFloorPacket(int entityId, int targetY) implements C2SPacket {
    public static final PacketCodec<ByteBuf, ElevatorTargetFloorPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ElevatorTargetFloorPacket::entityId,
        PacketCodecs.INTEGER,
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
    public PacketType<ElevatorTargetFloorPacket> getPacketType() {
        return AllPackets.ELEVATOR_SET_FLOOR;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ElevatorTargetFloorPacket> callback() {
        return AllHandle::onElevatorTargetFloor;
    }
}
