package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public record PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, PackagePortPlacementPacket> CODEC = PacketCodec.tuple(
        PackagePortTarget.PACKET_CODEC,
        PackagePortPlacementPacket::target,
        BlockPos.PACKET_CODEC,
        PackagePortPlacementPacket::pos,
        PackagePortPlacementPacket::new
    );

    @Override
    public void apply(ServerPlayPacketListener listener) {
        AllHandle.onPackagePortPlacement((ServerPlayNetworkHandler) listener, this);
    }

    @Override
    public PacketType<PackagePortPlacementPacket> getPacketType() {
        return AllPackets.PLACE_PACKAGE_PORT;
    }
}
