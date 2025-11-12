package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record SuperGlueSelectionPacket(BlockPos from, BlockPos to) implements C2SPacket {
    public static final StreamCodec<ByteBuf, SuperGlueSelectionPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SuperGlueSelectionPacket::from,
        BlockPos.STREAM_CODEC,
        SuperGlueSelectionPacket::to,
        SuperGlueSelectionPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SuperGlueSelectionPacket> type() {
        return AllPackets.GLUE_IN_AREA;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, SuperGlueSelectionPacket> callback() {
        return AllHandle::onSuperGlueSelection;
    }
}
