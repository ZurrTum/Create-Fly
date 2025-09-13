package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record ToolboxDisposeAllPacket(BlockPos toolboxPos) implements C2SPacket {
    public static final PacketCodec<ByteBuf, ToolboxDisposeAllPacket> CODEC = BlockPos.PACKET_CODEC.xmap(
        ToolboxDisposeAllPacket::new,
        ToolboxDisposeAllPacket::toolboxPos
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ToolboxDisposeAllPacket> getPacketType() {
        return AllPackets.TOOLBOX_DISPOSE_ALL;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ToolboxDisposeAllPacket> callback() {
        return AllHandle::onToolboxDisposeAll;
    }
}
