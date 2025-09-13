package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record FactoryPanelConnectionPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<FactoryPanelConnectionPacket> getPacketType() {
        return AllPackets.CONNECT_FACTORY_PANEL;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, FactoryPanelConnectionPacket> callback() {
        return AllHandle::onFactoryPanelConnection;
    }
}
