package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record SuperGlueRemovalPacket(int entityId, BlockPos soundSource) implements C2SPacket {
    public static final StreamCodec<ByteBuf, SuperGlueRemovalPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SuperGlueRemovalPacket::entityId,
        BlockPos.STREAM_CODEC,
        SuperGlueRemovalPacket::soundSource,
        SuperGlueRemovalPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SuperGlueRemovalPacket> type() {
        return AllPackets.GLUE_REMOVED;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, SuperGlueRemovalPacket> callback() {
        return AllHandle::onSuperGlueRemoval;
    }
}
