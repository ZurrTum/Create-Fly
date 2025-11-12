package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record WiFiEffectPacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, WiFiEffectPacket> CODEC = BlockPos.STREAM_CODEC.map(WiFiEffectPacket::new, WiFiEffectPacket::pos);

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<WiFiEffectPacket> type() {
        return AllPackets.PACKAGER_LINK_EFFECT;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, WiFiEffectPacket> callback() {
        return AllClientHandle::onWiFiEffect;
    }
}
