package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionRelocationPacket(int entityId) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ContraptionRelocationPacket> CODEC = ByteBufCodecs.INT.map(
        ContraptionRelocationPacket::new,
        ContraptionRelocationPacket::entityId
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionRelocationPacket> callback() {
        return AllClientHandle::onContraptionRelocation;
    }

    @Override
    public PacketType<ContraptionRelocationPacket> type() {
        return AllPackets.CONTRAPTION_RELOCATION;
    }
}
