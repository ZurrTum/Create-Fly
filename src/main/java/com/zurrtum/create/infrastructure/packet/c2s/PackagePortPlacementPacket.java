package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackagePortPlacementPacket> CODEC = StreamCodec.composite(
        PackagePortTarget.PACKET_CODEC,
        PackagePortPlacementPacket::target,
        BlockPos.STREAM_CODEC,
        PackagePortPlacementPacket::pos,
        PackagePortPlacementPacket::new
    );

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onPackagePortPlacement((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<PackagePortPlacementPacket> type() {
        return AllPackets.PLACE_PACKAGE_PORT;
    }
}
