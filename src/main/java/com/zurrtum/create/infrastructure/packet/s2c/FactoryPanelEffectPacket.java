package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record FactoryPanelEffectPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FactoryPanelEffectPacket> callback() {
        return AllClientHandle::onFactoryPanelEffect;
    }

    @Override
    public PacketType<FactoryPanelEffectPacket> getPacketType() {
        return AllPackets.FACTORY_PANEL_EFFECT;
    }
}
