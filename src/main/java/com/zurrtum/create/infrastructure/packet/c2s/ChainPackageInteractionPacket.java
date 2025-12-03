package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record ChainPackageInteractionPacket(
    BlockPos pos, BlockPos selectedConnection, float chainPosition, boolean removingPackage
) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ChainPackageInteractionPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ChainPackageInteractionPacket::pos,
        CatnipStreamCodecBuilders.nullable(BlockPos.PACKET_CODEC),
        ChainPackageInteractionPacket::selectedConnection,
        PacketCodecs.FLOAT,
        ChainPackageInteractionPacket::chainPosition,
        PacketCodecs.BOOLEAN,
        ChainPackageInteractionPacket::removingPackage,
        ChainPackageInteractionPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onChainPackageInteraction((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<ChainPackageInteractionPacket> getPacketType() {
        return AllPackets.CHAIN_PACKAGE_INTERACTION;
    }
}
