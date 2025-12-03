package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record RemoveBlockEntityPacket(BlockPos pos) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, RemoveBlockEntityPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        RemoveBlockEntityPacket::new,
        RemoveBlockEntityPacket::pos
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onRemoveBlockEntity(listener, this);
    }

    @Override
    public PacketType<RemoveBlockEntityPacket> getPacketType() {
        return AllPackets.REMOVE_TE;
    }
}
