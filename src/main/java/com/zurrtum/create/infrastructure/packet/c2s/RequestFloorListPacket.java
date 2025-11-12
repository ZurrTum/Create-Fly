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

public record RequestFloorListPacket(int entityId) implements C2SPacket {
    public static final StreamCodec<ByteBuf, RequestFloorListPacket> CODEC = ByteBufCodecs.INT.map(
        RequestFloorListPacket::new,
        RequestFloorListPacket::entityId
    );

    public RequestFloorListPacket(AbstractContraptionEntity entity) {
        this(entity.getId());
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<RequestFloorListPacket> type() {
        return AllPackets.REQUEST_FLOOR_LIST;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, RequestFloorListPacket> callback() {
        return AllHandle::onElevatorRequestFloorList;
    }
}
