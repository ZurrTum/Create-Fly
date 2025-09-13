package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record ChainPackageInteractionPacket(
    BlockPos pos, BlockPos selectedConnection, float chainPosition, boolean removingPackage
) implements C2SPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<ChainPackageInteractionPacket> getPacketType() {
        return AllPackets.CHAIN_PACKAGE_INTERACTION;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ChainPackageInteractionPacket> callback() {
        return AllHandle::onChainPackageInteraction;
    }
}
