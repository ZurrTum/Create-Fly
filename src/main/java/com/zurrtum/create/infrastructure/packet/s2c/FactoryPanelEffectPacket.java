package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record FactoryPanelEffectPacket(
    FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success
) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, FactoryPanelEffectPacket> CODEC = StreamCodec.composite(
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelEffectPacket::fromPos,
        FactoryPanelPosition.PACKET_CODEC,
        FactoryPanelEffectPacket::toPos,
        ByteBufCodecs.BOOL,
        FactoryPanelEffectPacket::success,
        FactoryPanelEffectPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onFactoryPanelEffect(listener, this);
    }

    @Override
    public PacketType<FactoryPanelEffectPacket> type() {
        return AllPackets.FACTORY_PANEL_EFFECT;
    }
}
