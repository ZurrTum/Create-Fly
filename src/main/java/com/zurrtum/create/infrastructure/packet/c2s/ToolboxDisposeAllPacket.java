package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ToolboxDisposeAllPacket(BlockPos toolboxPos) implements C2SPacket {
    public static final StreamCodec<ByteBuf, ToolboxDisposeAllPacket> CODEC = BlockPos.STREAM_CODEC.map(
        ToolboxDisposeAllPacket::new,
        ToolboxDisposeAllPacket::toolboxPos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ToolboxDisposeAllPacket> type() {
        return AllPackets.TOOLBOX_DISPOSE_ALL;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ToolboxDisposeAllPacket> callback() {
        return AllHandle::onToolboxDisposeAll;
    }
}
