package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record HighlightPacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, HighlightPacket> CODEC = BlockPos.PACKET_CODEC.xmap(HighlightPacket::new, HighlightPacket::pos);

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, HighlightPacket> callback() {
        return AllClientHandle::onHighlight;
    }

    @Override
    public PacketType<HighlightPacket> getPacketType() {
        return AllPackets.BLOCK_HIGHLIGHT;
    }
}
