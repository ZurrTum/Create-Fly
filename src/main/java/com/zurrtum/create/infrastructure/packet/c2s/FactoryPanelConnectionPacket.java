package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record FactoryPanelConnectionPacket(
    FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, FactoryPanelConnectionPacket> CODEC = StreamCodec.composite(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConnectionPacket::fromPos,
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelConnectionPacket::toPos,
        ByteBufCodecs.BOOL,
        FactoryPanelConnectionPacket::relocate,
        FactoryPanelConnectionPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onFactoryPanelConnection((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<FactoryPanelConnectionPacket> type() {
        return AllPackets.CONNECT_FACTORY_PANEL;
    }
}
