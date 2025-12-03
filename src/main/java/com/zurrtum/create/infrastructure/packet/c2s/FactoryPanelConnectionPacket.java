package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public record FactoryPanelConnectionPacket(
    FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, FactoryPanelConnectionPacket> CODEC = PacketCodec.tuple(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConnectionPacket::fromPos,
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConnectionPacket::toPos,
        PacketCodecs.BOOLEAN,
        FactoryPanelConnectionPacket::relocate,
        FactoryPanelConnectionPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onFactoryPanelConnection((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<FactoryPanelConnectionPacket> getPacketType() {
        return AllPackets.CONNECT_FACTORY_PANEL;
    }
}
