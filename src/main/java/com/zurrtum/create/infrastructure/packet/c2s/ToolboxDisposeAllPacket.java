package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ToolboxDisposeAllPacket(BlockPos toolboxPos) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, ToolboxDisposeAllPacket> CODEC = BlockPos.STREAM_CODEC.map(
        ToolboxDisposeAllPacket::new,
        ToolboxDisposeAllPacket::toolboxPos
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onToolboxDisposeAll((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ToolboxDisposeAllPacket> type() {
        return AllPackets.TOOLBOX_DISPOSE_ALL;
    }
}
