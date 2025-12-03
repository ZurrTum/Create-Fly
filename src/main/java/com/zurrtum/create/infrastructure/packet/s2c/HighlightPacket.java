package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record HighlightPacket(BlockPos pos) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, HighlightPacket> CODEC = BlockPos.PACKET_CODEC.xmap(HighlightPacket::new, HighlightPacket::pos);

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onHighlight(this);
    }

    @Override
    public PacketType<HighlightPacket> getPacketType() {
        return AllPackets.BLOCK_HIGHLIGHT;
    }
}
