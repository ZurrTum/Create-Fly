package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record SuperGlueSelectionPacket(BlockPos from, BlockPos to) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, SuperGlueSelectionPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        SuperGlueSelectionPacket::from,
        BlockPos.PACKET_CODEC,
        SuperGlueSelectionPacket::to,
        SuperGlueSelectionPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onSuperGlueSelection((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<SuperGlueSelectionPacket> getPacketType() {
        return AllPackets.GLUE_IN_AREA;
    }
}
