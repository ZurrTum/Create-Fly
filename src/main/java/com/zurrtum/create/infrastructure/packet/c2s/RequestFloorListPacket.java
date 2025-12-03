package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record RequestFloorListPacket(int entityId) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RequestFloorListPacket> CODEC = PacketCodecs.INTEGER.xmap(
        RequestFloorListPacket::new,
        RequestFloorListPacket::entityId
    );

    public RequestFloorListPacket(AbstractContraptionEntity entity) {
        this(entity.getId());
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onElevatorRequestFloorList((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<RequestFloorListPacket> getPacketType() {
        return AllPackets.REQUEST_FLOOR_LIST;
    }
}
