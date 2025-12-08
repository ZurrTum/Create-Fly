package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record RemoveBlockEntityPacket(BlockPos pos) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, RemoveBlockEntityPacket> CODEC = BlockPos.STREAM_CODEC.map(
        RemoveBlockEntityPacket::new,
        RemoveBlockEntityPacket::pos
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onRemoveBlockEntity(listener, this);
    }

    @Override
    public PacketType<RemoveBlockEntityPacket> type() {
        return AllPackets.REMOVE_TE;
    }
}
