package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record FactoryPanelEffectPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FactoryPanelEffectPacket> callback() {
        return AllClientHandle::onFactoryPanelEffect;
    }

    @Override
    public PacketType<FactoryPanelEffectPacket> type() {
        return AllPackets.FACTORY_PANEL_EFFECT;
    }
}
