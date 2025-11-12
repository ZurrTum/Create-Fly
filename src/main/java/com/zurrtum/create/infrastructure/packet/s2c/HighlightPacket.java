package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record HighlightPacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, HighlightPacket> CODEC = BlockPos.STREAM_CODEC.map(HighlightPacket::new, HighlightPacket::pos);

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, HighlightPacket> callback() {
        return AllClientHandle::onHighlight;
    }

    @Override
    public PacketType<HighlightPacket> type() {
        return AllPackets.BLOCK_HIGHLIGHT;
    }
}
