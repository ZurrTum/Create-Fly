package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record FactoryPanelEffectPacket(
    FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success
) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, FactoryPanelEffectPacket> CODEC = PacketCodec.tuple(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelEffectPacket::fromPos,
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelEffectPacket::toPos,
        PacketCodecs.BOOLEAN,
        FactoryPanelEffectPacket::success,
        FactoryPanelEffectPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onFactoryPanelEffect(listener, this);
    }

    @Override
    public PacketType<FactoryPanelEffectPacket> getPacketType() {
        return AllPackets.FACTORY_PANEL_EFFECT;
    }
}
