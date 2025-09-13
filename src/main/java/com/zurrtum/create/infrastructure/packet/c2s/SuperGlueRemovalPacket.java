package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record SuperGlueRemovalPacket(int entityId, BlockPos soundSource) implements C2SPacket {
    public static final PacketCodec<ByteBuf, SuperGlueRemovalPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        SuperGlueRemovalPacket::entityId,
        BlockPos.PACKET_CODEC,
        SuperGlueRemovalPacket::soundSource,
        SuperGlueRemovalPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<SuperGlueRemovalPacket> getPacketType() {
        return AllPackets.GLUE_REMOVED;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, SuperGlueRemovalPacket> callback() {
        return AllHandle::onSuperGlueRemoval;
    }
}
