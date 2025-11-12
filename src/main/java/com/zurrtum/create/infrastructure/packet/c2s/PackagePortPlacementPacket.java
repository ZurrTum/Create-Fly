package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackagePortPlacementPacket> CODEC = StreamCodec.composite(
        PackagePortTarget.PACKET_CODEC,
        PackagePortPlacementPacket::target,
        BlockPos.STREAM_CODEC,
        PackagePortPlacementPacket::pos,
        PackagePortPlacementPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public PacketType<PackagePortPlacementPacket> type() {
        return AllPackets.PLACE_PACKAGE_PORT;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, PackagePortPlacementPacket> callback() {
        return AllHandle::onPackagePortPlacement;
    }
}
