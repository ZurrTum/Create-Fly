package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record WiFiEffectPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, WiFiEffectPacket> CODEC = BlockPos.PACKET_CODEC.xmap(WiFiEffectPacket::new, WiFiEffectPacket::pos);

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<WiFiEffectPacket> getPacketType() {
        return AllPackets.PACKAGER_LINK_EFFECT;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, WiFiEffectPacket> callback() {
        return AllClientHandle::onWiFiEffect;
    }
}
