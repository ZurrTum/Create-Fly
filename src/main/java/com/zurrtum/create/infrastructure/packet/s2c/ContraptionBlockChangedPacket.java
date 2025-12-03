package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record ContraptionBlockChangedPacket(int entityId, BlockPos localPos, BlockState newState) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ContraptionBlockChangedPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ContraptionBlockChangedPacket::entityId,
        BlockPos.PACKET_CODEC,
        ContraptionBlockChangedPacket::localPos,
        CatnipStreamCodecs.BLOCK_STATE,
        ContraptionBlockChangedPacket::newState,
        ContraptionBlockChangedPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionBlockChanged(this);
    }

    @Override
    public PacketType<ContraptionBlockChangedPacket> getPacketType() {
        return AllPackets.CONTRAPTION_BLOCK_CHANGED;
    }
}
