package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record SuperGlueRemovalPacket(int entityId, BlockPos soundSource) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, SuperGlueRemovalPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SuperGlueRemovalPacket::entityId,
        BlockPos.STREAM_CODEC,
        SuperGlueRemovalPacket::soundSource,
        SuperGlueRemovalPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onSuperGlueRemoval((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<SuperGlueRemovalPacket> type() {
        return AllPackets.GLUE_REMOVED;
    }
}
