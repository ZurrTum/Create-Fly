package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record FactoryPanelConnectionPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<FactoryPanelConnectionPacket> type() {
        return AllPackets.CONNECT_FACTORY_PANEL;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, FactoryPanelConnectionPacket> callback() {
        return AllHandle::onFactoryPanelConnection;
    }
}
