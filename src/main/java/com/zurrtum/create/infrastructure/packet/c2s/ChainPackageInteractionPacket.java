package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ChainPackageInteractionPacket(
    BlockPos pos, BlockPos selectedConnection, float chainPosition, boolean removingPackage
) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<ByteBuf, ChainPackageInteractionPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ChainPackageInteractionPacket::pos,
        CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC),
        ChainPackageInteractionPacket::selectedConnection,
        ByteBufCodecs.FLOAT,
        ChainPackageInteractionPacket::chainPosition,
        ByteBufCodecs.BOOL,
        ChainPackageInteractionPacket::removingPackage,
        ChainPackageInteractionPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onChainPackageInteraction((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ChainPackageInteractionPacket> type() {
        return AllPackets.CHAIN_PACKAGE_INTERACTION;
    }
}
