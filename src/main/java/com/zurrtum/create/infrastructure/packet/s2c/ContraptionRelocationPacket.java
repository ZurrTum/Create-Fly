package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionRelocationPacket(int entityId) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ContraptionRelocationPacket> CODEC = PacketCodecs.INTEGER.xmap(
        ContraptionRelocationPacket::new,
        ContraptionRelocationPacket::entityId
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionRelocationPacket> callback() {
        return AllClientHandle::onContraptionRelocation;
    }

    @Override
    public PacketType<ContraptionRelocationPacket> getPacketType() {
        return AllPackets.CONTRAPTION_RELOCATION;
    }
}
