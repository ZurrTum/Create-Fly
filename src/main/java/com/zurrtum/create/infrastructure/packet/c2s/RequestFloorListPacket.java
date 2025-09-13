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

public record RequestFloorListPacket(int entityId) implements C2SPacket {
    public static final PacketCodec<ByteBuf, RequestFloorListPacket> CODEC = PacketCodecs.INTEGER.xmap(
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
    public PacketType<RequestFloorListPacket> getPacketType() {
        return AllPackets.REQUEST_FLOOR_LIST;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, RequestFloorListPacket> callback() {
        return AllHandle::onElevatorRequestFloorList;
    }
}
