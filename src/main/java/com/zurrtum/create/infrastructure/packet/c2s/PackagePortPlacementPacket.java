package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public record PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, PackagePortPlacementPacket> CODEC = PacketCodec.tuple(
        PackagePortTarget.PACKET_CODEC,
        PackagePortPlacementPacket::target,
        BlockPos.PACKET_CODEC,
        PackagePortPlacementPacket::pos,
        PackagePortPlacementPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<PackagePortPlacementPacket> getPacketType() {
        return AllPackets.PLACE_PACKAGE_PORT;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, PackagePortPlacementPacket> callback() {
        return AllHandle::onPackagePortPlacement;
    }
}
